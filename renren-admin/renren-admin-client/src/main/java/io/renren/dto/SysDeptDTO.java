/**
 * Copyright (c) 2018 人人开源 All rights reserved.
 * <p>
 * https://www.renren.io
 * <p>
 * 版权所有，侵权必究！
 */

package io.renren.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.renren.commons.tools.utils.DateUtils;
import io.renren.commons.tools.utils.TreeNode;
import io.renren.commons.tools.validator.group.AddGroup;
import io.renren.commons.tools.validator.group.DefaultGroup;
import io.renren.commons.tools.validator.group.UpdateGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 部门管理
 *
 * @author Mark sunlightcs@gmail.com
 * @since 1.0.0
 */
@Schema(description = "部门管理")
@Data
public class SysDeptDTO extends TreeNode implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "id")
    @Null(message = "{id.null}", groups = AddGroup.class)
    @NotNull(message = "{id.require}", groups = UpdateGroup.class)
    private Long id;

    @Schema(description = "上级ID")
    @NotNull(message = "{sysdept.pid.require}", groups = DefaultGroup.class)
    private Long pid;

    @Schema(description = "部门名称")
    @NotBlank(message = "{sysdept.name.require}", groups = DefaultGroup.class)
    private String name;

    @Schema(description = "负责人ID")
    private Long leaderId;

    @Schema(description = "负责人名称")
    private String leaderName;

    @Schema(description = "排序")
    @Min(value = 0, message = "{sort.number}", groups = DefaultGroup.class)
    private Integer sort;


    /**
     * 机构配置
     */
    @Schema(description = "apiDomain")
    private String apiDomain;
    @Schema(description = "币种")
    private String currency;
    @Schema(description = "时区")
    private String timezone;
    @Schema(description = "botKey")
    private String botKey;
    @Schema(description = "botName")
    private String botName;
    @Schema(description = "运营模式")
    private String processMode;
    @Schema(description = "充值点位")
    private BigDecimal antChargeRate;
    @Schema(description = "提现点位")
    private BigDecimal outRate;
    @Schema(description = "一级分佣")
    private BigDecimal c1Rate;
    @Schema(description = "二级分佣")
    private BigDecimal c2Rate;

    @Schema(description = "创建时间")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @JsonFormat(pattern = DateUtils.DATE_TIME_PATTERN)
    private Date createDate;

    @Schema(description = "上级部门名称")
    private String parentName;

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setSort(Integer sort) {
        this.sort = sort;
    }

    public Integer getSort() {
        return sort;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public String getParentName() {
        return parentName;
    }

    public void setParentName(String parentName) {
        this.parentName = parentName;
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public Long getPid() {
        return pid;
    }

    @Override
    public void setPid(Long pid) {
        this.pid = pid;
    }

    public Long getLeaderId() {
        return leaderId;
    }

    public void setLeaderId(Long leaderId) {
        this.leaderId = leaderId;
    }

    public String getLeaderName() {
        return leaderName;
    }

    public void setLeaderName(String leaderName) {
        this.leaderName = leaderName;
    }
}