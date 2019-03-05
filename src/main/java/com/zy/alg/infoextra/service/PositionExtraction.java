package com.zy.alg.infoextra.service;

import java.util.List;

import com.zy.alg.infoextra.utils.OutputPosiInfo;

/**
 * Geographical location extract
 *
 * @author zhangyu
 */
public interface PositionExtraction {

    /**
     * universal position infomation extract
     *
     * @param text (text corpus)
     * @return
     */
    List<OutputPosiInfo> uniPosiExtra(String text);
}
