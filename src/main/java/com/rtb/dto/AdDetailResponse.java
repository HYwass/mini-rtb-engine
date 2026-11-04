package com.rtb.dto;


import com.rtb.entity.Ad;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class AdDetailResponse {

    private Ad ad;
    private List<TargetItem> targets;
}
