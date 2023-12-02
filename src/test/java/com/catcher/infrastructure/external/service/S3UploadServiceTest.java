package com.catcher.infrastructure.external.service;

import com.amazonaws.services.kms.model.AWSKMSException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.catcher.app.AppApplication;
import com.catcher.common.BaseResponseStatus;
import com.catcher.common.exception.BaseException;
import com.catcher.infrastructure.utils.KmsUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ActiveProfiles("test")
@SpringBootTest(classes = AppApplication.class)
public class S3UploadServiceTest {

    @Mock
    private AmazonS3 amazonS3;

    @Mock
    private KmsUtils kmsUtils;

    @InjectMocks
    S3UploadService s3UploadService;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    private MultipartFile multipartFile;

    @BeforeEach
    void beforeEach() throws IOException {
        multipartFile = generateFile();
    }

    @DisplayName("복호화 과정에서 잘못된 AWSKMSException 던진다")
    @Test
    void decrypt_error() throws IOException {
        //given
        when(kmsUtils.decrypt(Mockito.any())).thenThrow(AWSKMSException.class);

        //when

        //then
        var result = Assertions.assertThrows(BaseException.class, () -> {
            s3UploadService.uploadFile(multipartFile);
        });
        assertThat(result.getStatus()).isEqualTo(BaseResponseStatus.KMS_ERROR);
    }

    @DisplayName("multipartFile.getInputStream()에서 IOException 발생")
    @Test
    void multipart_file_get_input_stream_error() throws IOException {
        //given
        when(multipartFile.getInputStream()).thenThrow(IOException.class);

        //when

        //then
        var result = Assertions.assertThrows(BaseException.class, () -> {
            s3UploadService.uploadFile(multipartFile);
        });
        assertThat(result.getStatus()).isEqualTo(BaseResponseStatus.AWS_IO_ERROR);
    }

    @DisplayName("S3에 파일 업로드하면서 에러가 발생하면 AmazonS3Exception 발생한다")
    @Test
    void s3_upload_error() {
        //given
        when(amazonS3.putObject(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
                .thenThrow(AmazonS3Exception.class);

        //when

        //then
        var result = Assertions.assertThrows(BaseException.class, () -> {
            s3UploadService.uploadFile(multipartFile);
        });
        assertThat(result.getStatus()).isEqualTo(BaseResponseStatus.S3UPLOAD_ERROR);
    }

    @Test
    void success_upload_file() throws MalformedURLException {
        //given
        URL s3Url = new URL("https://example.com/");

        when(amazonS3.putObject(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(null);
        when(amazonS3.getUrl(Mockito.any(), Mockito.any())).thenReturn(s3Url);

        //when
        String uploadedFileUrl = s3UploadService.uploadFile(multipartFile);

        //then
        assertThat(uploadedFileUrl).isEqualTo(s3Url.toString());
    }

    private MultipartFile generateFile() throws IOException {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getSize()).thenReturn(50L);
        when(file.getContentType()).thenReturn(MediaType.MULTIPART_FORM_DATA_VALUE);
        when(file.getInputStream()).thenReturn(InputStream.nullInputStream());

        return file;
    }
}