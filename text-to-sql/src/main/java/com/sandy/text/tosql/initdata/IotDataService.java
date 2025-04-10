package com.sandy.text.tosql.initdata;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class IotDataService {

    @Autowired
    private IotDataRepository repository;

    @Transactional
    public void generateIotData() {
        ZonedDateTime startTime = ZonedDateTime.parse("2025-04-01T00:00:00+08:00");
        List<IotData> batch = new ArrayList<>();
        int batchSize = 1000;
        BigDecimal currentValue = new BigDecimal("1000");
        int max = 24 * 10;
        for (int i = 0; i < max; i++) {
            ZonedDateTime currentTime = startTime.plusHours(i);
            batch.add(new IotData("1号锅炉用水量", currentTime, currentValue));
            currentValue = currentValue.add(new BigDecimal("100"));
            // Save in batches
            if (batch.size() >= batchSize) {
                repository.saveAll(batch);
                batch.clear();
            }
        }

        // Save remaining items
        if (!batch.isEmpty()) {
            repository.saveAll(batch);
        }

        System.out.println("Inserted 100,000 rows into iotdata table.");
    }
}
