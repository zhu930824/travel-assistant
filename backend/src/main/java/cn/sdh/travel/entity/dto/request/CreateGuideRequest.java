package cn.sdh.travel.entity.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class CreateGuideRequest {

    @NotBlank(message = "标题不能为空")
    @Size(max = 100, message = "标题最多100个字符")
    private String title;

    @NotBlank(message = "目的地不能为空")
    @Size(max = 100, message = "目的地最多100个字符")
    private String destination;

    @NotBlank(message = "内容不能为空")
    private String content;

    private String coverImage;

    private List<String> images;

    private List<String> tags;

    private Integer status = 1;
}
