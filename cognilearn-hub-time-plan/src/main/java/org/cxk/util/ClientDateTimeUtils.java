package org.cxk.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * @author KJH
 * @description
 * @create 2025/11/15 09:42
 */
public class ClientDateTimeUtils {
    // 方法1：明确传递时区
    public static Instant getClientDayStart(Instant instant, String clientTimeZone) {

        // 使用客户端时区解析
        ZoneId clientZone = ZoneId.of(clientTimeZone);
        ZonedDateTime clientTime = instant.atZone(clientZone);

        // 获取客户端时区的0点
        LocalDate clientDate = clientTime.toLocalDate();
        return clientDate.atStartOfDay(clientZone).toInstant();
    }
}
