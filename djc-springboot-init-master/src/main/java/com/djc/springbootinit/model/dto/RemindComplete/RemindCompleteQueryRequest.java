package com.djc.springbootinit.model.dto.RemindComplete;

import com.djc.springbootinit.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 查询请求
 *

 */
@EqualsAndHashCode(callSuper = true)
@Data
public class RemindCompleteQueryRequest extends PageRequest implements Serializable {

    /**
     * 学生id
     */
    private Long studentId;

    private static final long serialVersionUID = 1L;
}