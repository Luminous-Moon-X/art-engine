package com.art.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.art.art.common.CommonDataVO;
import com.art.art.common.CommonSearchVO;
import com.art.art.exception.ArtException;
import com.art.art.kits.ConvertUtil;
import com.art.art.kits.WrapperUtil;
import com.art.domain.SysNav;
import com.art.domain.vo.MenuMetaVO;
import com.art.domain.vo.NavTreeVO;
import com.art.domain.vo.SystemNavVO;
import com.art.mapper.SysNavMapper;
import com.art.service.SysNavService;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;

import org.springframework.context.annotation.DependsOn;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.art.domain.table.SysNavTableDef.SYS_NAV;

/**
 * 导航表Service实现类
 *
 * @author Luminous X.
 * @since 0.0.1
 */
@Slf4j
@Service
@AllArgsConstructor
@DependsOn("hikariLoader")
public class SysNavServiceImpl extends ServiceImpl<SysNavMapper, SysNav> implements SysNavService {
    /**
     * Redis模板
     */
    private final RedisTemplate<String, String> redisTemplate;

    /**
     * 分页查询导航
     *
     * @param commonSearchVO 通用查询对象
     * @return 分页数据对象
     */
    @Override
    public Page<SystemNavVO> queryPage(CommonSearchVO commonSearchVO) {
        // 分页查询最上级导航信息
        QueryWrapper wrapper = QueryWrapper.create();
        wrapper.eq("parent_id", "-1");
        wrapper.orderBy("sort_num", true);
        Page<SysNav> originPage = this.page(new Page<>(commonSearchVO.getPageNum(), commonSearchVO.getPageSize()),
                WrapperUtil.buildWrapper(wrapper, commonSearchVO));
        List<SysNav> records = originPage.getRecords();
        List<SystemNavVO> navVOList = this.transformAndQueryChild(records);
        // 重新封装返回前端
        Page<SystemNavVO> finalPage = new Page<>(originPage.getPageNumber(), originPage.getPageSize(),
                originPage.getTotalRow());
        finalPage.setRecords(navVOList);
        return finalPage;
    }

    /**
     * 获取当前用户导航信息
     *
     * @return 当前用户导航信息
     */
    @Override
    public List<NavTreeVO> navTree() {
        return this.getNavTree();
    }

    /**
     * 根据父级ID查询子导航信息
     *
     * @param parentId 父级ID
     * @return 子导航集合
     */
    @Override
    public List<SystemNavVO> queryChildren(String parentId) {
        QueryWrapper wrapper = QueryWrapper.create();
        wrapper.eq(SysNav::getParentId, parentId);
        wrapper.orderBy(SysNav::getSortNum, true);
        List<SysNav> navList = this.list(wrapper);
        return this.transformAndQueryChild(navList);
    }

    /**
     * 新增导航
     *
     * @param commonDataVO 通用数据VO类
     * @return 新增结果
     */
    @Override
    public Boolean insertData(CommonDataVO commonDataVO) {
        SystemNavVO systemNavVO = JSONObject.parseObject(JSON.toJSONString(commonDataVO.getData()), SystemNavVO.class);
        if (systemNavVO == null) {
            throw new ArtException("表单数据为空，请检查！");
        }
        SysNav sysNav = ConvertUtil.convert(systemNavVO, SysNav.class);
        return this.save(sysNav);
    }

    /**
     * 修改导航
     *
     * @param commonDataVO 通用数据VO类
     * @return 修改结果
     */
    @Override
    public Boolean updateData(CommonDataVO commonDataVO) {
        SystemNavVO systemNavVO = JSONObject.parseObject(JSON.toJSONString(commonDataVO.getData()), SystemNavVO.class);
        if (systemNavVO == null) {
            throw new ArtException("表单数据为空，请检查！");
        }
        SysNav sysNav = ConvertUtil.convert(systemNavVO, SysNav.class);
        return this.updateById(sysNav);
    }

    /**
     * 转换导航数据类型并判断子导航
     *
     * @param originList 原始导航集合
     * @return 转换后的导航集合
     */
    private List<SystemNavVO> transformAndQueryChild(List<SysNav> originList) {
        List<SystemNavVO> navVOList = ConvertUtil.convertList(originList, SystemNavVO.class);
        navVOList.parallelStream().forEach(nav -> {
            QueryWrapper countWrapper = QueryWrapper.create();
            countWrapper.eq(SysNav::getParentId, nav.getId());
            long count = this.count(countWrapper);
            nav.setHasChildren(count > 0);
        });
        return navVOList;
    }

    /**
     * 服务启动自动初始化菜单缓存
     */
    @PostConstruct
    public void initMenuCache() {
        log.info("初始化菜单树结构数据缓存");
        long start = System.currentTimeMillis();
        List<NavTreeVO> menuTreeList = this.queryMenuTreeDB();
        redisTemplate.opsForValue().set("navTree", JSON.toJSONString(menuTreeList));
        long end = System.currentTimeMillis();
        log.info("菜单树结构数据缓存初始化成功，耗时：{}ms", end - start);
    }

    /**
     * 获取导航树信息
     *
     * @return 导航树信息
     */
    private List<NavTreeVO> getNavTree() {
        List<NavTreeVO> rootTreeList;
        // 先查缓存
        if (redisTemplate.hasKey("navTree")) {
            rootTreeList = JSONArray.parseArray(redisTemplate.opsForValue().get("navTree"), NavTreeVO.class);
        } else {
            rootTreeList = this.queryMenuTreeDB();
        }
        // 更新缓存
        redisTemplate.opsForValue().set("navTree", JSON.toJSONString(rootTreeList));
        return rootTreeList;
    }

    /**
     * 从数据库查询菜单树信息
     *
     * @return 菜单树信息
     */
    private List<NavTreeVO> queryMenuTreeDB() {
        // 查询根节点导航
        QueryWrapper wrapper = QueryWrapper.create()
                .select()
                .from(SYS_NAV)
                .where(SYS_NAV.PARENT_ID.eq(-1))
                .orderBy(SYS_NAV.SORT_NUM, true);
        List<SysNav> rootNav = this.list(wrapper);
        List<NavTreeVO> rootTreeList = rootNav.stream()
                .map(item -> new NavTreeVO(item.getId(), String.valueOf(item.getId()), item.getNavPath(),
                        item.getComponent(), new MenuMetaVO(item.getNavIcon(), item.getNavName(), item.getCacheFlag(), item.getFixedFlag()), null))
                .toList();
        // 递归处理子节点，组装成树结构
        rootTreeList.forEach(this::handleTree);
        return rootTreeList;
    }

    /**
     * 递归处理子节点
     *
     * @param navTree 导航节点对象
     */
    private void handleTree(NavTreeVO navTree) {
        Long id = navTree.getIndex();
        QueryWrapper wrapper = QueryWrapper.create();
        wrapper.eq(SysNav::getParentId, id);
        List<SysNav> children = this.list(wrapper);
        if (!CollectionUtil.isEmpty(children)) {
            List<NavTreeVO> childTreeList = children.stream().map(item -> new NavTreeVO(item.getId(), item.getNavName(),
                    item.getNavPath(), item.getComponent(), new MenuMetaVO(item.getNavIcon(), item.getNavName(), item.getCacheFlag(), item.getFixedFlag()), null))
                    .toList();
            childTreeList.forEach(this::handleTree);
            navTree.setChildren(childTreeList);
        }
    }
}
