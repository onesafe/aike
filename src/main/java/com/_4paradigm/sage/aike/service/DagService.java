package com._4paradigm.sage.aike.service;

import com._4paradigm.sage.aike.entity.DagDMO;
import com._4paradigm.sage.aike.entity.DagDTO;
import com._4paradigm.sage.aike.mapper.DagMapper;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by wangyiping on 2019/12/10 5:14 PM.
 */
@Repository
public class DagService {

    @Autowired
    private DagMapper dagMapper;

    public void insertDB(MultipartFile file, String dagName) throws Exception {
        InputStream fileInputStream = file.getInputStream();
        String dagContent = IOUtils.toString(fileInputStream, StandardCharsets.UTF_8);

        // 先检查dagname是否存在
        List<DagDTO> dagDTOS = list();
        for (DagDTO dagDTO : dagDTOS) {
            if (dagName.equals(dagDTO.getDagName())) {
                throw new Exception("Dag Name already exist");
            }
        }

        DagDMO dagDMO = new DagDMO();
        dagDMO.setDagContent(dagContent);
        dagDMO.setDagName(dagName);
        dagMapper.insert(dagDMO);
    }


    public DagDTO select(Long dagID) {
        DagDMO dagDMO = dagMapper.select(dagID);
        DagDTO dagDTO = new DagDTO();
        dagDTO.setId(dagDMO.getId());
        dagDTO.setDagName(dagDMO.getDagName());
        return dagDTO;
    }

    public List<DagDTO> list() {
        List<DagDMO> dagDMOS = dagMapper.list();

        List<DagDTO> dagDTOS = new ArrayList<>();

        for (DagDMO dagDMO : dagDMOS) {
            DagDTO dagDTO = new DagDTO();
            dagDTO.setDagName(dagDMO.getDagName());
            dagDTO.setId(dagDMO.getId());
            dagDTOS.add(dagDTO);
        }
        return dagDTOS;
    }

    public void delete(Long dagID) {
        dagMapper.delete(dagID);
    }
}
