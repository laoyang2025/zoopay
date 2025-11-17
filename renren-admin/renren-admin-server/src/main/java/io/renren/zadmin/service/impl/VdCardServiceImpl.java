package io.renren.zadmin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.renren.commons.mybatis.annotation.DataFilter;
import io.renren.commons.mybatis.service.impl.CrudServiceImpl;
import io.renren.commons.tools.constant.Constant;
import io.renren.commons.tools.page.PageData;
import io.renren.zadmin.dao.VdCardDao;
import io.renren.zadmin.dto.VdCardDTO;
import io.renren.zadmin.entity.VdCardEntity;
import io.renren.zadmin.service.VdCardService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * VIEW
 *
 * @author Mark sunlightcs@gmail.com
 * @since 3.0 2024-09-10
 */
@Service
public class VdCardServiceImpl extends CrudServiceImpl<VdCardDao, VdCardEntity, VdCardDTO> implements VdCardService {

    @DataFilter(userId = "")
    @Override
    public PageData<VdCardDTO> page(Map<String, Object> params) {
        IPage<VdCardEntity> page = baseDao.selectPage(
                getPage(params, Constant.CREATE_DATE, false),
                applyFilter(params)
        );
        return getPageData(page, VdCardDTO.class);
    }

    @DataFilter(userId = "")
    @Override
    public List<VdCardDTO> list(Map<String, Object> params) {
        return super.list(params);
    }

    @Override
    public QueryWrapper<VdCardEntity> getWrapper(Map<String, Object> params) {
        QueryWrapper<VdCardEntity> wrapper = new QueryWrapper<>();

        String cardNo = (String) params.get("cardNo");
        wrapper.eq(StringUtils.isNotBlank(cardNo), "card_no", cardNo);

        String createDate = (String) params.get("createDate");
        wrapper.eq(StringUtils.isNotBlank(createDate), "create_date", createDate);

        return wrapper;
    }


}