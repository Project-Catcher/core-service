package com.catcher.infrastructure;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.kms.AWSKMS;
import com.amazonaws.services.kms.AWSKMSClientBuilder;
import com.amazonaws.services.kms.model.DecryptRequest;
import com.amazonaws.services.kms.model.EncryptRequest;
import com.amazonaws.services.kms.model.EncryptionAlgorithmSpec;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

@Slf4j
@Service
public class KmsService {
    @Value("${aws.kms.keyId}")
    private static String KEY_ID;

    @Value("${spring.profiles.active}")
    private static String PROFILE;

    public String encrypt(String text) {
        AWSKMS kmsClient = AWSKMSClientBuilder.standard()
                .withCredentials(new ProfileCredentialsProvider(PROFILE))
                .withRegion(Regions.AP_NORTHEAST_2)
                .build();

        EncryptRequest request = new EncryptRequest();
        request.withKeyId(KEY_ID);
        request.withPlaintext(ByteBuffer.wrap(text.getBytes(StandardCharsets.UTF_8)));
        request.withEncryptionAlgorithm(EncryptionAlgorithmSpec.SYMMETRIC_DEFAULT);

        byte[] cipherBytes = kmsClient.encrypt(request).getCiphertextBlob().array();
        return Base64.encodeBase64String(cipherBytes);
    }

    public String decrypt(String cipherBase64) {
        AWSKMS kmsClient = AWSKMSClientBuilder.standard()
                .withCredentials(new ProfileCredentialsProvider(PROFILE))
                .withRegion(Regions.AP_NORTHEAST_2)
                .build();

        DecryptRequest request = new DecryptRequest();
        request.withKeyId(KEY_ID);
        request.withCiphertextBlob(ByteBuffer.wrap(Base64.decodeBase64(cipherBase64)));
        request.withEncryptionAlgorithm(EncryptionAlgorithmSpec.SYMMETRIC_DEFAULT);

        byte[] textBytes = kmsClient.decrypt(request).getPlaintext().array();
        return new String(textBytes);
    }
}
