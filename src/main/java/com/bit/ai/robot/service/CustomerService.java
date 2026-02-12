package com.bit.ai.robot.service;


import com.bit.ai.robot.model.vo.customerService.DeleteMarkdownFileReqVO;
import com.bit.ai.robot.model.vo.customerService.FindMarkdownFilePageListReqVO;
import com.bit.ai.robot.model.vo.customerService.FindMarkdownFilePageListRspVO;
import com.bit.ai.robot.model.vo.customerService.UpdateMarkdownFileReqVO;
import com.bit.ai.robot.utils.PageResponse;
import com.bit.ai.robot.utils.Response;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author: sunny
 * @url: www.bit.com
 * @date: 2025/7/11 23:03
 * @description: AI 客服
 **/
public interface CustomerService {

    /**
     * 上传 Markdown 问答文件
     * @param file
     * @return
     */
    Response<?> uploadMarkdownFile(MultipartFile file);

    /**
     * 删除 Markdown 问答文件
     * @param deleteMarkdownFileReqVO
     * @return
     */
    Response<?> deleteMarkdownFile(DeleteMarkdownFileReqVO deleteMarkdownFileReqVO);

    /**
     * 分页查询 Markdown 问答文件
     * @param findMarkdownFilePageListReqVO
     * @return
     */
    PageResponse<FindMarkdownFilePageListRspVO> findMarkdownFilePageList(FindMarkdownFilePageListReqVO findMarkdownFilePageListReqVO);

    /**
     * 修改  Markdown 问答文件信息
     * @param updateMarkdownFileReqVO
     * @return
     */
    Response<?> updateMarkdownFile(UpdateMarkdownFileReqVO updateMarkdownFileReqVO);
}
