package com.sandy.text.tosql.initdata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class IotDataService {

    @Autowired
    private IotDataRepository repository;

    @Transactional
    public void generateIotData() {
        ZonedDateTime startTime = ZonedDateTime.parse("2025-04-09T00:00:00+08:00");
        List<IotData> batch = new ArrayList<>();
        int batchSize = 1000;
        BigDecimal currentValue = new BigDecimal("10000");
        for (int i = 0; i < 10000; i++) {
            ZonedDateTime currentTime = startTime.minusHours(i);
            batch.add(new IotData("1号锅炉用水量", currentTime, currentValue));
            currentValue = currentValue.subtract(new BigDecimal("1.7"));
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
