package com.lkd.http.controller;

import com.alibaba.excel.EasyExcel;
import com.lkd.entity.OrderCollectEntity;
import com.lkd.http.viewModel.BillExportDataVO;
import com.lkd.service.OrderService;
import com.lkd.service.ReportService;
import com.lkd.viewmodel.Pager;
import lombok.RequiredArgsConstructor;
import lombok.var;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Date;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@RequestMapping("/report")
@RestController
public class ReportController {


    private final ReportService reportService;
    private final OrderService orderService;


    /**
     * 获取一定日期范围之内的合作商分成汇总数据
     *
     * @param pageIndex
     * @param pageSize
     * @param partnerName
     * @param start
     * @param end
     * @return
     */
    @GetMapping("/partnerCollect")
    public Pager<OrderCollectEntity> getPartnerCollect(
            @RequestParam(value = "pageIndex", required = false, defaultValue = "1") Long pageIndex,
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") Long pageSize,
            @RequestParam(value = "partnerName", required = false, defaultValue = "") String partnerName,
            @RequestParam(value = "start", required = true, defaultValue = "")
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate start,
            @RequestParam(value = "end", required = true, defaultValue = "")
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end
    ) {
        return reportService.getPartnerCollect(pageIndex, pageSize, partnerName, start, end);
    }

    /**
     * 获取最近12条分账信息
     *
     * @param partnerId
     * @return
     */
    @GetMapping("/top12Collect/{partnerId}")
    public List<OrderCollectEntity> getTop12Collect(@PathVariable Integer partnerId) {
        return reportService.getTop12(partnerId);
    }

    /**
     * 合作商搜索分账信息
     *
     * @param partnerId
     * @param pageIndex
     * @param pageSize
     * @param nodeName
     * @param start
     * @param end
     * @return
     */
    @GetMapping("/search/{partnerId}")
    public Pager<OrderCollectEntity> search(
            @PathVariable Integer partnerId,
            @RequestParam(value = "pageIndex", required = false, defaultValue = "1") Long pageIndex,
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") Long pageSize,
            @RequestParam(value = "nodeName", required = false, defaultValue = "") String nodeName,
            @RequestParam(value = "start", required = true, defaultValue = "") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate start,
            @RequestParam(value = "end", required = true, defaultValue = "") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end) {
        return reportService.search(
                pageIndex,
                pageSize,
                partnerId,
                nodeName,
                start,
                end);
    }

    /**
     * 数据导出
     *
     * @param partnerId
     * @param start
     * @param end
     */
    @GetMapping("/export/{partnerId}/{start}/{end}")
    public void export(
            HttpServletResponse response,
            @PathVariable Integer partnerId,
            @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate start,
            @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end,
            @RequestParam(value = "nodeName", required = false, defaultValue = "") String nodeName) throws IOException {

        var exportData = reportService.getList(partnerId, nodeName, start, end)
                .stream().map(item -> {
                    var vo = new BillExportDataVO();
                    vo.setAmount(item.getTotalBill());
                    vo.setDate(Date.from(item.getDate().atTime(0, 0).atZone(ZoneId.systemDefault()).toInstant()));
                    vo.setOrderCount(item.getOrderCount());
                    vo.setNodeName(item.getNodeName());
                    return vo;
                }).collect(Collectors.toList());

        response.setContentType("application/vnd.ms-excel");
        response.setCharacterEncoding("utf-8");
        response.setHeader("Content-disposition", "attachment;filename=bill.xlsx");
        EasyExcel.write(response.getOutputStream(), BillExportDataVO.class).sheet("分账数据").doWrite(exportData);
    }
}
