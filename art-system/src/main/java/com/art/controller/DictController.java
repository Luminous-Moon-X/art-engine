package com.art.controller;

import com.art.common.HttpResult;
import com.art.common.TableRowVO;
import com.art.domain.Dict;
import com.art.domain.vo.DictVO;
import com.art.service.DictService;
import com.mybatisflex.core.paginate.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 字典控制器
 *
 * @author Luminous.X
 * @since 1.0.0
 */
@RestController
@RequestMapping("/dict")
@Tag(name = "字典管理", description = "字典管理相关接口")
public class DictController {
    /**
     * 字典服务
     */
    private final DictService DictService;

    /**
     * 构造函数
     *
     * @param DictService 字典服务
     */
    public DictController(DictService DictService) {
        this.DictService = DictService;
    }

    /**
     * 根据ID获取字典信息
     *
     * @param id 字典ID
     * @return 字典信息
     */
    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取字典信息", description = "根据ID获取字典信息")
    public HttpResult<Dict> getById(@PathVariable("id") Long id) {
        return HttpResult.success(this.DictService.selectById(id));
    }

    /**
     * 分页查询字典信息
     *
     * @param page 分页对象
     * @param vo   查询条件对象
     * @return 分页数据对象
     */
    @PostMapping("/page")
    @Operation(summary = "分页查询字典信息", description = "分页查询字典信息")
    public HttpResult<Page<DictVO>> page(Page<DictVO> page, DictVO vo) {
        return HttpResult.success(this.DictService.queryPage(page, vo));
    }

    /**
     * 查询所有字典信息
     *
     * @return 字典信息列表
     */
    @GetMapping("/list")
    @Operation(summary = "查询所有字典信息", description = "查询所有字典信息")
    public HttpResult<List<Dict>> list() {
        return HttpResult.success(this.DictService.selectList());
    }

    /**
     * 新增字典
     *
     * @param vo 字典信息
     * @return 新增结果
     */
    @PostMapping("/add")
    @Operation(summary = "新增字典", description = "新增字典")
    public HttpResult<Boolean> add(@RequestBody DictVO vo) {
        return HttpResult.success(DictService.add(vo));
    }

    /**
     * 编辑字典
     *
     * @param vo 字典信息
     * @return 编辑结果
     */
    @PutMapping("/edit")
    @Operation(summary = "编辑字典", description = "编辑字典")
    public HttpResult<Boolean> edit(@RequestBody DictVO vo) {
        return HttpResult.success(DictService.edit(vo));
    }

    /**
     * 根据ID删除字典
     *
     * @param tableRowVO 表格行VO类
     * @return 删除结果
     */
    @DeleteMapping("/delete")
    @Operation(summary = "删除字典", description = "根据ID删除字典")
    public HttpResult<Boolean> delete(@RequestBody TableRowVO tableRowVO) {
        return HttpResult.success(this.DictService.delete(tableRowVO.getIdList()));
    }

}
