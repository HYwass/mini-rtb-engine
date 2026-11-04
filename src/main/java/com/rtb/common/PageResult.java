package com.rtb.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class PageResult<T> {

    private final long total;
    private final int pageNum;
    private final int pageSize;
    private final List<T> list;
}
