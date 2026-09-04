package com.smarthis.common.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> {

    private List<T> records;
    private long total;
    private int page;
    private int size;

    public int getTotalPages() {
        return size > 0 ? (int) Math.ceil((double) total / size) : 0;
    }
}
