package com.smarthis.pharma.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smarthis.pharma.entity.InventoryBatch;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;

@Mapper
public interface InventoryBatchMapper extends BaseMapper<InventoryBatch> {
    @Update("""
            UPDATE pha_inventory_batch
               SET quantity = quantity - #{quantity},
                   available_quantity = available_quantity - #{quantity},
                   version = version + 1,
                   updated_time = CURRENT_TIMESTAMP
             WHERE id = #{batchId}
               AND deleted = 0
               AND is_active = 1
               AND expiry_date >= CURRENT_DATE
               AND available_quantity >= #{quantity}
            """)
    int deductAvailable(@Param("batchId") Long batchId, @Param("quantity") BigDecimal quantity);

    @Update("""
            UPDATE pha_inventory_batch
               SET quantity = quantity + #{quantity},
                   available_quantity = available_quantity + #{quantity},
                   version = version + 1,
                   updated_time = CURRENT_TIMESTAMP
             WHERE id = #{batchId} AND deleted = 0 AND is_active = 1
            """)
    int addAvailable(@Param("batchId") Long batchId, @Param("quantity") BigDecimal quantity);

    @Update("""
            UPDATE pha_inventory_batch
               SET quantity = #{quantity},
                   available_quantity = #{quantity},
                   version = version + 1,
                   updated_time = CURRENT_TIMESTAMP
             WHERE id = #{batchId} AND deleted = 0 AND is_active = 1 AND version = #{version}
            """)
    int setAvailable(@Param("batchId") Long batchId, @Param("quantity") BigDecimal quantity,
                     @Param("version") Integer version);
}
