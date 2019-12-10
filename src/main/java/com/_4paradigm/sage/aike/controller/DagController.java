package com._4paradigm.sage.aike.controller;

import com._4paradigm.sage.aike.entity.DagDTO;
import com._4paradigm.sage.aike.io.Response;
import com._4paradigm.sage.aike.service.DagService;
import io.undertow.util.StatusCodes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Created by wangyiping on 2019/12/9 4:39 PM.
 */
@RestController
@RequestMapping("/aike/v1/dag")
public class DagController {

    @Autowired
    private DagService dagService;

    @PostMapping
    public ResponseEntity<Response<String>> upload(@RequestParam MultipartFile file, @RequestParam String dagName) {
        Response<String> res = new Response<>();

        try {
            dagService.insertDB(file, dagName);

            res.setCode(StatusCodes.CREATED);
            res.setData("Upload dag success");
            return new ResponseEntity<>(res, HttpStatus.CREATED);
        } catch (Exception e) {

            res.setCode(StatusCodes.INTERNAL_SERVER_ERROR);
            res.setMessage(e.getMessage());
            return new ResponseEntity<>(res, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{dagID}")
    public ResponseEntity<Response<DagDTO>> getDag(@PathVariable Long dagID) {
        try {
            DagDTO dagDTO = dagService.select(dagID);

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
            List<DagDTO> dagDTOS = dagService.list();

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
            dagService.delete(dagID);

            Response<String> res = new Response<>();
            res.setData("Delete dag " + dagID + "success");
            res.setCode(StatusCodes.OK);
            return new ResponseEntity<>(res, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(new Response<>(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
