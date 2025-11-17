package io.renren.zadmin.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.renren.commons.tools.utils.DateUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
* VIEW
*
* @author Mark sunlightcs@gmail.com
* @since 3.0 2024-09-10
*/
@Data
@Schema(description = "VIEW")
public class VdCardDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private BigDecimal successAmount;
    private BigDecimal success;
    private BigDecimal fail;
    private BigDecimal successRate;
    private Date createDate;
    private String cardNo;
    private String cardUser;
    private String deptName;
    private Long deptId;
}