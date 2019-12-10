package com._4paradigm.sage.aike.controller;

import com._4paradigm.sage.aike.config.Constants;
import com._4paradigm.sage.aike.entity.BatchPredictorDTO;
import com._4paradigm.sage.aike.entity.DagDMO;
import com._4paradigm.sage.aike.entity.RunStrategyType;
import com._4paradigm.sage.aike.io.Response;
import com._4paradigm.sage.aike.mapper.DagMapper;
import com._4paradigm.sage.sdk.v1.common.util.DateUtil;
import com._4paradigm.sage.sdk.v1.common.util.SerDesUtil;
import com._4paradigm.sage.sdk.v1.data.domain.TableGroup;
import com._4paradigm.sage.sdk.v1.data.enumeration.GroupType;
import com._4paradigm.sage.sdk.v1.data.filter.model.BaseModelFilter;
import com._4paradigm.sage.sdk.v1.data.service.DataService;
import com._4paradigm.sage.sdk.v1.data.util.GroupFilterUtil;
import com._4paradigm.sage.sdk.v1.solution.domain.InputConfig;
import com._4paradigm.sage.sdk.v1.solution.domain.OutputConfig;
import com._4paradigm.sage.sdk.v1.solution.domain.ScheduleConfig;
import com._4paradigm.sage.sdk.v1.solution.domain.Solution;
import com._4paradigm.sage.sdk.v1.solution.service.BatchPredictService;
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

import java.util.Map;

/**
 * Created by wangyiping on 2019/12/10 4:08 PM.
 */
@RestController
@RequestMapping("/aike/v1/batchpredictor")
@SuppressWarnings("all")
public class BatchPredictorController {

    @Autowired
    private DagMapper dagMapper;

    @PostMapping
    public ResponseEntity<Response<String>> createBatchPredictor(
            @RequestBody BatchPredictorDTO batchPredictorDTO,
            @RequestHeader("Access-Key") String accessKey,
            @RequestHeader("Workspace-ID") Integer workspaceID
    ) {
        String NAME_SPACE = "sdkServer";

        try {
            BatchPredictService batchPredictService = new BatchPredictService(
                    Constants.sdkServerUrl,
                    accessKey,
                    workspaceID
            );

            DataService dataService = new DataService(
                    Constants.sdkServerUrl,
                    accessKey,
                    workspaceID
            );

            DagDMO dagDMO = dagMapper.select(batchPredictorDTO.getDagID());
            System.out.println(dagDMO.getDagContent());
            Map<String, String> solutionMap = SerDesUtil.deserializeFromJson(dagDMO.getDagContent(), Map.class);
            FeDag feDag = SerDesUtil.deserializeFromJson(SerDesUtil.serializeAsJsonString(solutionMap.get("dag")), FeDag.class);
            BeDag beDag = DagConvertUtil.toBeDag(feDag);


            /**
             * 封装用于发布的Solution对象
             */
            Solution solution = new Solution();
            solution.setName(batchPredictorDTO.getSolutionName());
            solution.setDescribe(batchPredictorDTO.getSolutionDescribe());
            solution.setTimeoutInSeconds(10000L);
            solution.setDag(beDag);

            System.out.println(feDag.getNodes());

            String dataSliceID = "";
            for(FeNode feNode : feDag.getNodes()) {
                if ("DataSlice".equals(feNode.getName()) && "MODELGROUP".equals(feNode.getType())) {
                    dataSliceID = feNode.getId();
                }
            }


            /**
             * 配置输入节点参数
             * 自学习业务中经常以数据组做为输入，在此代码示例中，取DataSlice节点id
             */
            InputConfig modelGroupInput = new InputConfig();
            modelGroupInput.setNodeId(dataSliceID);
            BaseModelFilter modelGroupFilter = GroupFilterUtil.createLastModelFilter();
            modelGroupInput.setGroupFilter(modelGroupFilter);
            solution.setInputConfigs(Lists.newArrayList(modelGroupInput));

            /**
             * 创建输出模型组，如果模型组已经存在可以跳过此步骤（先知前端没有可以创建模型组入口）
             */
            TableGroup batchPredictOutputTableGroup = dataService.createTableGroup(NAME_SPACE, batchPredictorDTO.getOutputTableGroupName());

            /**
             * 配置输出节点参数
             * 自学习业务中，需要将模型输出到模型组中，在此示例中取PicoTraining节点id
             */
            String modelPredictionID = "";
            for(FeNode feNode : feDag.getNodes()) {
                if ("ModelPrediction".equals(feNode.getOperatorName())) {
                    modelPredictionID = feNode.getId();
                    System.out.println(modelPredictionID);
                }
            }
            OutputConfig modelOutput = new OutputConfig(batchPredictorDTO.getOutputTableGroupName(), modelPredictionID, GroupType.TABLE);
            solution.setOutputConfigs(Lists.newArrayList(modelOutput));

            /**
             * 配置运行策略
             */
            Long startTime = DateUtil.toDate("yyyy-MM-dd HH:mm:ss", batchPredictorDTO.getRunStrategy().getData().getStartTime()).getTime();
            Long endTime = DateUtil.toDate("yyyy-MM-dd HH:mm:ss", batchPredictorDTO.getRunStrategy().getData().getEndTime()).getTime();
            Long interval = batchPredictorDTO.getRunStrategy().getData().getInterval();

            ScheduleConfig scheduleConfig = null;
            if (RunStrategyType.fromValue(batchPredictorDTO.getRunStrategy().getType()) == RunStrategyType.scheduleRunByTimePeriod) {
                scheduleConfig = ScheduleConfigUtil.buildScheduleRunByTimePeriod(startTime,endTime, interval);
            }
            solution.setScheduleConfig(scheduleConfig);

            /**
             * 提交solution，返回solution会带有solutionId，可以用作后续对solution操作
             */
            solution = batchPredictService.submitSolution(solution);
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



    @DeleteMapping("/{batchpredictorID}")
    public ResponseEntity<Response<String>> deleteBatchPredictor(
            @PathVariable Long batchpredictorID,
            @RequestHeader("Access-Key") String accessKey,
            @RequestHeader("Workspace-ID") Integer workspaceID
    ) {
        try {
            BatchPredictService batchPredictService = new BatchPredictService(
                    Constants.sdkServerUrl,
                    accessKey,
                    workspaceID
            );

            Solution solution = new Solution();
            boolean deleteSuccess = batchPredictService.deleteSolution(batchpredictorID);
            if (deleteSuccess) {
                Response<String> res = new Response<>();
                res.setCode(StatusCodes.OK);
                res.setData("Delete solution " + batchpredictorID + " success");
                return new ResponseEntity<>(res, HttpStatus.OK);
            } else {
                Response<String> res = new Response<>();
                res.setCode(StatusCodes.INTERNAL_SERVER_ERROR);
                res.setData("Delete solution " + batchpredictorID + " failed");
                return new ResponseEntity<>(res, HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } catch (Exception e) {
            Response<String> res = new Response<>();
            res.setCode(StatusCodes.INTERNAL_SERVER_ERROR);
            res.setMessage(e.getMessage());
            return new ResponseEntity<>(res, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
