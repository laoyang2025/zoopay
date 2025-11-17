package io.renren.zadmin.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.baomidou.mybatisplus.annotation.*;

import java.math.BigDecimal;
import java.util.Date;

import io.renren.commons.mybatis.entity.BaseEntity;

/**
 * VIEW
 *
 * @author Mark sunlightcs@gmail.com
 * @since 3.0 2024-09-10
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("vd_card")
public class VdCardEntity {
    private static final long serialVersionUID = 1L;

    private BigDecimal successAmount;
    private BigDecimal success;
    private BigDecimal fail;
    private BigDecimal successRate;
    private String deptName;
    private Long deptId;

    private Date createDate;
    private String cardNo;
    private String cardUser;
}