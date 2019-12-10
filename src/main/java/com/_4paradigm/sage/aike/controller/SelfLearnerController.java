package com._4paradigm.sage.aike.controller;

import com._4paradigm.sage.aike.config.Constants;
import com._4paradigm.sage.aike.entity.DagDMO;
import com._4paradigm.sage.aike.entity.DagDTO;
import com._4paradigm.sage.aike.entity.RunStrategyType;
import com._4paradigm.sage.aike.entity.SelfLearnerDTO;
import com._4paradigm.sage.aike.io.Response;
import com._4paradigm.sage.aike.mapper.DagMapper;
import com._4paradigm.sage.sdk.v1.common.dto.PageDTO;
import com._4paradigm.sage.sdk.v1.common.util.DateUtil;
import com._4paradigm.sage.sdk.v1.common.util.SerDesUtil;
import com._4paradigm.sage.sdk.v1.data.domain.ModelGroup;
import com._4paradigm.sage.sdk.v1.data.enumeration.GroupType;
import com._4paradigm.sage.sdk.v1.data.filter.table.SliceTableFilter;
import com._4paradigm.sage.sdk.v1.data.service.DataService;
import com._4paradigm.sage.sdk.v1.data.util.GroupFilterUtil;
import com._4paradigm.sage.sdk.v1.solution.domain.*;
import com._4paradigm.sage.sdk.v1.solution.service.SelfLearnService;
import com._4paradigm.sage.sdk.v1.solution.util.ScheduleConfigUtil;
import com._4paradigm.sage.sdk.v1.workflow.domain.be.BeDag;
import com._4paradigm.sage.sdk.v1.workflow.domain.fe.FeDag;
import com._4paradigm.sage.sdk.v1.workflow.domain.fe.FeNode;
import com._4paradigm.sage.sdk.v1.workflow.util.DagConvertUtil;
import com.google.common.collect.Lists;
import io.undertow.util.StatusCodes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Created by wangyiping on 2019/12/9 4:23 PM.
 */
@RestController
@RequestMapping("/aike/v1/selflearner")
@SuppressWarnings("all")
public class SelfLearnerController {

    @Autowired
    private DagMapper dagMapper;

    @PostMapping
    public ResponseEntity<Response<String>> createSelfLearner(
            @RequestBody SelfLearnerDTO selfLearnerDTO,
            @RequestHeader("Access-Key") String accessKey,
            @RequestHeader("Workspace-ID") Integer workspaceID
    ) {
        String NAME_SPACE = "sdkServer";

        try {
            SelfLearnService selfLearnService = new SelfLearnService(
                    Constants.sdkServerUrl,
                    accessKey,
                    workspaceID
            );

            DataService dataService = new DataService(
                    Constants.sdkServerUrl,
                    accessKey,
                    workspaceID
            );

            DagDMO dagDMO = dagMapper.select(selfLearnerDTO.getDagID());
            System.out.println(dagDMO.getDagContent());
            Map<String, String> solutionMap = SerDesUtil.deserializeFromJson(dagDMO.getDagContent(), Map.class);
            FeDag feDag = SerDesUtil.deserializeFromJson(SerDesUtil.serializeAsJsonString(solutionMap.get("dag")), FeDag.class);
            BeDag beDag = DagConvertUtil.toBeDag(feDag);


            /**
             * 封装用于发布的Solution对象
             */
            Solution solution = new Solution();
            solution.setName(selfLearnerDTO.getSolutionName());
            solution.setDescribe(selfLearnerDTO.getSolutionDescribe());
            solution.setTimeoutInSeconds(10000L);
            solution.setDag(beDag);

            System.out.println(feDag.getNodes());

            String dataSliceID = "";
            for(FeNode feNode : feDag.getNodes()) {
                if ("DataSlice".equals(feNode.getName())) {
                    dataSliceID = feNode.getId();
                }
            }


            /**
             * 配置输入节点参数
             * 自学习业务中经常以数据组做为输入，在此代码示例中，取DataSlice节点id
             */
            InputConfig tableGroupInput = new InputConfig();
            tableGroupInput.setNodeId(dataSliceID);
            SliceTableFilter sliceTableFilter = GroupFilterUtil.createSliceTableFilter(1, 100);
            tableGroupInput.setGroupFilter(sliceTableFilter);
            solution.setInputConfigs(Lists.newArrayList(tableGroupInput));

            /**
             * 创建输出模型组，如果模型组已经存在可以跳过此步骤（先知前端没有可以创建模型组入口）
             */
            ModelGroup selfLearnerOutputModelGroup = dataService.createModelGroup(
                    NAME_SPACE, selfLearnerDTO.getOutputModelGroupName()
            );


            /**
             * 配置输出节点参数
             * 自学习业务中，需要将模型输出到模型组中，在此示例中取PicoTraining节点id
             */
            String picoTrainingID = "";
            for(FeNode feNode : feDag.getNodes()) {
                if ("PicoTraining".equals(feNode.getOperatorName())) {
                    picoTrainingID = feNode.getId();
                    System.out.println(picoTrainingID);
                }
            }
            OutputConfig modelOutput = new OutputConfig(selfLearnerDTO.getOutputModelGroupName(), picoTrainingID, GroupType.MODEL);
            solution.setOutputConfigs(Lists.newArrayList(modelOutput));

            /**
             * 配置运行策略
             */
            Long startTime = DateUtil.toDate("yyyy-MM-dd HH:mm:ss", selfLearnerDTO.getRunStrategy().getData().getStartTime()).getTime();
            Long endTime = DateUtil.toDate("yyyy-MM-dd HH:mm:ss", selfLearnerDTO.getRunStrategy().getData().getEndTime()).getTime();
            Long interval = selfLearnerDTO.getRunStrategy().getData().getInterval();

            ScheduleConfig scheduleConfig = null;
            if (RunStrategyType.fromValue(selfLearnerDTO.getRunStrategy().getType()) == RunStrategyType.scheduleRunByTimePeriod) {
                scheduleConfig = ScheduleConfigUtil.buildScheduleRunByTimePeriod(startTime,endTime, interval);
            }
            //scheduleConfig = ScheduleConfigUtil.buildImmediateRun();
            solution.setScheduleConfig(scheduleConfig);

            /**
             * 提交solution，返回solution会带有solutionId，可以用作后续对solution操作
             */
            solution = selfLearnService.submitSolution(solution);
            System.out.println(solution.getId());

            Response<String> res = new Response<>();
            res.setCode(StatusCodes.CREATED);
            res.setData("Solution ID is: " + solution.getId());
            return new ResponseEntity<>(res, HttpStatus.CREATED);
        } catch (Exception e) {
            Response<String> res = new Response<>();
            res.setCode(StatusCodes.INTERNAL_SERVER_ERROR);
            res.setMessage(e.getMessage());
            return new ResponseEntity<>(res, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @GetMapping("/{selflearnerID}")
    public ResponseEntity<Response<Solution>> getSelfLearner(
            @PathVariable Long selflearnerID,
            @RequestHeader("Access-Key") String accessKey,
            @RequestHeader("Workspace-ID") Integer workspaceID
    ) {
        try {
            SelfLearnService selfLearnService = new SelfLearnService(
                    Constants.sdkServerUrl,
                    accessKey,
                    workspaceID
            );

            Solution solution = new Solution();
            solution = selfLearnService.getSolution(selflearnerID);
            List<ExecuteHistory> executeHistories = selfLearnService.getHistory(selflearnerID);
            System.out.println(executeHistories);


            Response<Solution> res = new Response<>();
            res.setCode(StatusCodes.OK);
            res.setData(solution);
            return new ResponseEntity<>(res, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(new Response<>(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @DeleteMapping("/{selflearnerID}")
    public ResponseEntity<Response<String>> deleteSelfLearner(
            @PathVariable Long selflearnerID,
            @RequestHeader("Access-Key") String accessKey,
            @RequestHeader("Workspace-ID") Integer workspaceID
    ) {
        try {
            SelfLearnService selfLearnService = new SelfLearnService(
                    Constants.sdkServerUrl,
                    accessKey,
                    workspaceID
            );

            Solution solution = new Solution();
            boolean deleteSuccess = selfLearnService.deleteSolution(selflearnerID);
            if (deleteSuccess) {
                Response<String> res = new Response<>();
                res.setCode(StatusCodes.OK);
                res.setData("Delete solution " + selflearnerID + " success");
                return new ResponseEntity<>(res, HttpStatus.OK);
            } else {
                Response<String> res = new Response<>();
                res.setCode(StatusCodes.INTERNAL_SERVER_ERROR);
                res.setData("Delete solution " + selflearnerID + " failed");
                return new ResponseEntity<>(res, HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } catch (Exception e) {
            Response<String> res = new Response<>();
            res.setCode(StatusCodes.INTERNAL_SERVER_ERROR);
            res.setMessage(e.getMessage());
            return new ResponseEntity<>(res, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }



    @GetMapping
    public ResponseEntity<PageDTO<Solution>> getSelfLearners(
            @RequestHeader("Access-Key") String accessKey,
            @RequestHeader("Workspace-ID") Integer workspaceID,
            @RequestParam("pageNum") Integer pageNum,
            @RequestParam("pageSize") Integer pageSize
    ) {
        try {
            SelfLearnService selfLearnService = new SelfLearnService(
                    Constants.sdkServerUrl,
                    accessKey,
                    workspaceID
            );

            PageDTO<Solution> pageDTOSolutions = selfLearnService.listSolutions(pageNum, pageSize);

            return new ResponseEntity<>(pageDTOSolutions, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(new PageDTO<Solution>(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
