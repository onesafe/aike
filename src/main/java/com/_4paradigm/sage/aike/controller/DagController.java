package com._4paradigm.sage.aike.controller;

import com._4paradigm.sage.aike.entity.DagDMO;
import com._4paradigm.sage.aike.entity.DagDTO;
import com._4paradigm.sage.aike.io.Response;
import com._4paradigm.sage.aike.mapper.DagMapper;
import io.undertow.util.StatusCodes;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by wangyiping on 2019/12/9 4:39 PM.
 */
@RestController
@RequestMapping("/aike/v1/dag")
public class DagController {

    @Autowired
    private DagMapper dagMapper;

    @PostMapping
    public ResponseEntity<Response<String>> upload(@RequestParam MultipartFile file, @RequestParam String dagName) {
        InputStream fileInputStream = null;
        try {
            fileInputStream = file.getInputStream();
            String dagContent = IOUtils.toString(fileInputStream, StandardCharsets.UTF_8);

            DagDMO dagDMO = new DagDMO();
            dagDMO.setDagContent(dagContent);
            dagDMO.setDagName(dagName);
            dagMapper.insert(dagDMO);

            Response<String> res = new Response<>();
            res.setCode(StatusCodes.CREATED);
            res.setData("Upload dag success");
            return new ResponseEntity<>(res, HttpStatus.CREATED);
        } catch (IOException e) {
            return new ResponseEntity<>(new Response<>(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{dagID}")
    public ResponseEntity<Response<DagDTO>> getDag(@PathVariable Long dagID) {
        try {
            DagDMO dagDMO = dagMapper.select(dagID);
            DagDTO dagDTO = new DagDTO();
            dagDTO.setId(dagDMO.getId());
            dagDTO.setDagName(dagDMO.getDagName());

            Response<DagDTO> res = new Response<>();
            res.setData(dagDTO);
            res.setCode(StatusCodes.OK);
            return new ResponseEntity<>(res, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(new Response<>(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping
    public ResponseEntity<Response<List<DagDTO>>> listDag() {
        try {
            List<DagDMO> dagDMOS = dagMapper.list();

            List<DagDTO> dagDTOS = new ArrayList<>();

            for (DagDMO dagDMO : dagDMOS) {
                DagDTO dagDTO = new DagDTO();
                dagDTO.setDagName(dagDMO.getDagName());
                dagDTO.setId(dagDMO.getId());
                dagDTOS.add(dagDTO);
            }

            Response<List<DagDTO>> res = new Response<>();

            res.setData(dagDTOS);
            res.setCode(StatusCodes.OK);
            return new ResponseEntity<>(res, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(new Response<>(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/{dagID}")
    public ResponseEntity<Response<String>> deleteDag(@PathVariable Long dagID) {
        try {
            dagMapper.delete(dagID);

            Response<String> res = new Response<>();
            res.setData("Delete dag " + dagID + "success");
            res.setCode(StatusCodes.OK);
            return new ResponseEntity<>(res, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(new Response<>(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
