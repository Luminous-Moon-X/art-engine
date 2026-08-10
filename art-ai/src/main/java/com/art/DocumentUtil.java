package com.art;

import com.agentsflex.doc.DocumentExtractors;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

/**
 * 文档工具类
 *
 * @author Luminous.X
 * @since 1.3.0
 */
public class DocumentUtil {
    /**
     * 文档内容提取
     *
     * @param document 文档
     * @return 文档内容
     */
    public static String extract(File document) {
        return DocumentExtractors.extract(document);
    }

    /**
     * 文档内容提取
     *
     * @param document 文档
     * @return 文档内容
     * @throws IOException IO异常
     */
    public static String extract(MultipartFile document) throws IOException {
        String originalFilename = document.getOriginalFilename();
        String tempDir = System.getProperty("java.io.tmpdir");
        File targetFile = new File(tempDir + File.separator + originalFilename);
        document.transferTo(targetFile);
        return extract(targetFile);
    }
}
