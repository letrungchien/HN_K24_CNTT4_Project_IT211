package org.example.project_webservice_it211.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ClusterDTO {

    @NotBlank(message = "Tên cụm sân không được để trống")
    private String name;

    @NotBlank(message = "Địa chỉ không được để trống")
    private String address;

    private String hotLine;
}
