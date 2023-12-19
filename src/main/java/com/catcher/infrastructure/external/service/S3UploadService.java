package com.catcher.infrastructure.external.service;

import com.amazonaws.services.kms.model.AWSKMSException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.catcher.common.BaseResponseStatus;
import com.catcher.common.exception.BaseException;
import com.catcher.infrastructure.utils.KmsUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3UploadService {
    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    private final AmazonS3 amazonS3;
    private final KmsUtils kmsUtils;

    public String uploadFile(MultipartFile multipartFile) {
        String fileName = UUID.randomUUID().toString();

        try {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(multipartFile.getSize());
            metadata.setContentType(multipartFile.getContentType());

            String decryptedBucketName = kmsUtils.decrypt(bucket);

            amazonS3.putObject(
                    new PutObjectRequest(
                            decryptedBucketName,
                            fileName,
                            multipartFile.getInputStream(),
                            metadata
                    ).withCannedAcl(CannedAccessControlList.PublicRead)
            );

            return amazonS3.getUrl(decryptedBucketName, fileName).toString();
        } catch (AWSKMSException awskmsException) {
            throw new BaseException(BaseResponseStatus.KMS_ERROR);
        } catch (AmazonS3Exception s3Exception) {
            throw new BaseException(BaseResponseStatus.S3UPLOAD_ERROR);
        } catch (IOException exception) {
            throw new BaseException(BaseResponseStatus.AWS_IO_ERROR);
        }
    }
}
