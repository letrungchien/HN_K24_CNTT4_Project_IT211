package org.example.project_webservice_it211.dto.response;

import lombok.Data;
import org.example.project_webservice_it211.entity.BadmintonCluster;

@Data
public class ClusterResponse {
    private Long id;
    private String name;
    private String address;
    private String hotLine;


    private Long managerId;
    private String managerName;

    public static ClusterResponse from(BadmintonCluster cluster) {
        ClusterResponse dto = new ClusterResponse();
        dto.setId(cluster.getId());
        dto.setName(cluster.getName());
        dto.setAddress(cluster.getAddress());
        dto.setHotLine(cluster.getHotLine());
        if (cluster.getManager() != null) {
            dto.setManagerId(cluster.getManager().getId());
            dto.setManagerName(cluster.getManager().getFullName());
        }
        return dto;
    }
}
