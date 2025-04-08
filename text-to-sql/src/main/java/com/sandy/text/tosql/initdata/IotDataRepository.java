package com.sandy.text.tosql.initdata;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IotDataRepository extends JpaRepository<IotData, IotDataId> {
}