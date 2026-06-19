package com.movie.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.movie.dto.MovieVO;
import com.movie.entity.Crew;
import java.util.Map;

public interface CrewService {

    /**
     * 搜索主创人员（按姓名模糊查询）
     */
    Page<Crew> searchCrew(String keyword, int page, int size);

    /**
     * 获取主创详情
     */
    Crew getCrewDetail(Long crewId);

    /**
     * 获取主创参与的电影
     */
    Page<MovieVO> getMoviesByCrewId(Long crewId, int page, int size);

    /**
     * 获取主创的作品（按角色分类）
     */
    Map<String, Object> getMoviesByCrewIdWithRoles(Long crewId);
}
