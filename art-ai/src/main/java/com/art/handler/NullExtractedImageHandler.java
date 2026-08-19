package com.art.handler;

import com.agentsflex.doc.handler.ExtractedImageHandler;

/**
 * 忽略文档中提取到的图片
 *
 * @author Luminous.X
 * @since 1.3.2
 */
public class NullExtractedImageHandler implements ExtractedImageHandler {

    @Override
    public String handle(byte[] imageBytes, String mimeType, String fileName) {
        return null;
    }
}
