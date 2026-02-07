package com.overdue.web.controller.item;

import com.overdue.common.constant.Constants;
import com.overdue.common.core.controller.BaseController;
import com.overdue.common.core.domain.AjaxResult;
import com.overdue.framework.config.LocalDataUtil;
import com.overdue.manager.item.domain.entity.PersonalItem;
import com.overdue.manager.item.domain.entity.SharedItem;
import com.overdue.manager.item.service.PersonalItemService;
import com.overdue.manager.item.service.SharedItemService;
import com.overdue.manager.item.service.SharedSpaceService;
import com.overdue.manager.item.domain.entity.SharedSpace;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * ???????Controller
 * 
 * @author overdue
 */
@Api(tags = "???????-???????")
@RestController
@RequestMapping("/item/calendar")
public class CalendarController extends BaseController {
    @Autowired
    private PersonalItemService personalItemService;
    
    @Autowired
    private SharedItemService sharedItemService;
    
    @Autowired
    private SharedSpaceService sharedSpaceService;
    

    /**
     * ????????????????????
     * 
     * @param type ?????all/personal/shared
     * @param startDate ??????? YYYY-MM-DD???????
     * @param endDate ???????? YYYY-MM-DD???????
     * @param date ??????? YYYY-MM-DD???????
     * @return ?????????????????
     */
    @ApiOperation("????????????????????")
    @GetMapping("/items")
    public AjaxResult getItemsByDate(
            @RequestParam(required = false, defaultValue = "all") String type,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String date) {
        try {
            Map<String, Object> result = new HashMap<>();
            Map<String, List<Map<String, Object>>> itemsByDate = new HashMap<>();
            
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate start = null;
            LocalDate end = null;
            LocalDate specificDate = null;
            
            if (date != null && !date.isEmpty()) {
                specificDate = LocalDate.parse(date, formatter);
                start = specificDate;
                end = specificDate;
            } else {
                if (startDate != null && !startDate.isEmpty()) {
                    start = LocalDate.parse(startDate, formatter);
                }
                if (endDate != null && !endDate.isEmpty()) {
                    end = LocalDate.parse(endDate, formatter);
                }
            }
            
            // 用户ID：小程序端为 Overdue 用户 ID（ThreadLocal），后台为登录管理员 ID
            Long userId = null;
            Object overdueId = LocalDataUtil.getVar(Constants.OVERDUE_USER_ID);
            if (overdueId != null) {
                if (overdueId instanceof Long) userId = (Long) overdueId;
                else if (overdueId instanceof Number) userId = ((Number) overdueId).longValue();
                else try { userId = Long.parseLong(overdueId.toString()); } catch (NumberFormatException ignored) {}
            }
            if (userId == null) {
                try {
                    userId = getUserId();
                } catch (Exception e) {
                    return AjaxResult.error(401, "请先登录");
                }
            }

            // 个人物品??
            if (type.equals("all") || type.equals("personal")) {
                PersonalItem query = new PersonalItem();
                query.setUserId(userId);
                query.setStatus("0");
                List<PersonalItem> personalItems = personalItemService.selectPersonalItemList(query);
                
                for (PersonalItem item : personalItems) {
                    if (item.getExpiryDate() == null) continue;
                    
                    LocalDate expiryDate = item.getExpiryDate();
                    
                    // ?????????????????????????
                    if (start != null && expiryDate.isBefore(start)) continue;
                    if (end != null && expiryDate.isAfter(end)) continue;
                    
                    String dateKey = expiryDate.format(formatter);
                    itemsByDate.computeIfAbsent(dateKey, k -> new ArrayList<>()).add(convertPersonalItem(item));
                }
            }
            
            // 获取共享物品（需要根据用户加入的空间获取）
            if (type.equals("all") || type.equals("shared")) {
                // 根据用户ID获取用户加入的所有空间
                List<SharedSpace> userSpaces = sharedSpaceService.selectByUserId(userId);
                
                for (SharedSpace space : userSpaces) {
                    List<SharedItem> sharedItems = sharedItemService.selectBySpaceId(space.getId());
                    
                    for (SharedItem item : sharedItems) {
                        if (item.getExpiryDate() == null || !"0".equals(item.getStatus())) continue;
                        
                        LocalDate expiryDate = item.getExpiryDate();
                        
                        // 如果指定了日期范围，进行过滤
                        if (start != null && expiryDate.isBefore(start)) continue;
                        if (end != null && expiryDate.isAfter(end)) continue;
                        
                        String dateKey = expiryDate.format(formatter);
                        itemsByDate.computeIfAbsent(dateKey, k -> new ArrayList<>()).add(convertSharedItem(item));
                    }
                }
            }
            
            result.put("itemsByDate", itemsByDate);
            result.put("totalDates", itemsByDate.size());
            
            int totalItems = itemsByDate.values().stream()
                    .mapToInt(List::size)
                    .sum();
            result.put("totalItems", totalItems);
            
            return AjaxResult.success(result);
        } catch (Exception e) {
            logger.error("?????????????????", e);
            return AjaxResult.error("???????????" + e.getMessage());
        }
    }
    
    /**
     * ???????????Map
     */
    private Map<String, Object> convertPersonalItem(PersonalItem item) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", item.getId());
        map.put("name", item.getName());
        map.put("category", item.getCategory());
        map.put("expiryDate", item.getExpiryDate() != null ? item.getExpiryDate().toString() : null);
        map.put("productionDate", item.getProductionDate() != null ? item.getProductionDate().toString() : null);
        map.put("purchaseDate", item.getPurchaseDate() != null ? item.getPurchaseDate().toString() : null);
        map.put("shelfLife", item.getShelfLife());
        map.put("shelfLifeUnit", item.getShelfLifeUnit());
        map.put("type", "personal");
        return map;
    }
    
    /**
     * ???????????Map
     */
    private Map<String, Object> convertSharedItem(SharedItem item) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", item.getId());
        map.put("spaceId", item.getSpaceId());
        map.put("name", item.getName());
        map.put("category", item.getCategory());
        map.put("expiryDate", item.getExpiryDate() != null ? item.getExpiryDate().toString() : null);
        map.put("productionDate", item.getProductionDate() != null ? item.getProductionDate().toString() : null);
        map.put("purchaseDate", item.getPurchaseDate() != null ? item.getPurchaseDate().toString() : null);
        map.put("shelfLife", item.getShelfLife());
        map.put("shelfLifeUnit", item.getShelfLifeUnit());
        map.put("type", "shared");
        return map;
    }
}
