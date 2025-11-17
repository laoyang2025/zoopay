package io.renren.zadmin.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.renren.commons.tools.utils.DateUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
* z_sms
*
* @author Mark sunlightcs@gmail.com
* @since 3.0 2024-09-10
*/
@Data
@Schema(description = "z_sms")
public class ZSmsDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    @Schema(description = "机构id")
    private Long deptId;
    @Schema(description = "机构名称")
    private String deptName;
    private String content;
    private String phone;
    private String deviceId;
    private String md5;

    String utr;
    BigDecimal Amount;
    Integer matchStatus;
    Integer failCount;
    Long cardId;
    Long chargeId;

    @Schema(description = "创建者")
    private Long creator;
    @Schema(description = "创建时间")
    private Date createDate;
    @Schema(description = "更新者")
    private Long updater;
    @Schema(description = "更新时间")
    private Date updateDate;

}