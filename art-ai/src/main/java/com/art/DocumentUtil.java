package com.art;

import com.agentsflex.core.document.Document;
import com.agentsflex.core.document.id.RandomIdGenerator;
import com.agentsflex.core.document.splitter.AIDocumentSplitter;
import com.agentsflex.core.document.splitter.RegexDocumentSplitter;
import com.agentsflex.core.model.chat.ChatModel;
import com.agentsflex.doc.DocumentExtractors;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * 文档工具类
 *
 * @author Luminous.X
 * @since 1.3.0
 */
@SuppressWarnings("unused")
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

    /**
     * 文档拆分，LLM语义拆分
     *
     * @param document 文档
     * @param model    大模型
     * @return 拆分后的文档列表
     */
    public static List<Document> splitAi(Document document, ChatModel model) {
        AIDocumentSplitter splitter = new AIDocumentSplitter(model);
        return splitter.split(document);
    }

    /**
     * 文档拆分，按照段落拆分
     *
     * @param document 文档
     * @return 拆分后的文档列表
     */
    public static List<Document> splitParagraph(Document document) {
        RegexDocumentSplitter splitter = new RegexDocumentSplitter("\\n");
        return splitter.split(document, new RandomIdGenerator());
    }

    /**
     * 文档拆分，按照正则表达式拆分
     *
     * @param document 文档
     * @param regex    正则表达式
     * @return 拆分后的文档列表
     */
    public static List<Document> splitRegex(Document document, String regex) {
        RegexDocumentSplitter splitter = new RegexDocumentSplitter(regex);
        return splitter.split(document);
    }
}
