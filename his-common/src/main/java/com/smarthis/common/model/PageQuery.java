package com.smarthis.common.model;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;

@Data
public class PageQuery {
    private int page = 1;
    private int size = 20;

    public <T> Page<T> toPage() {
        return new Page<>(page, size);
    }
}
