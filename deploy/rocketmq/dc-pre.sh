#!/bin/bash

#set -x

function prepare() {
    echo ">>>>>>> rocketmq docker cluster prepare ..."
    # 1 环境变量校验
    if [[ ${ROCKETMQ_SERVER_IMAGE} == "" ]] ; then
        echo "env ROCKETMQ_SERVER_IMGAE not set yet !"
        exit
    fi
    if [[ ${ROCKETMQ_BROKER_IMAGE} == "" ]] ; then
        echo "env ROCKETMQ_SERVER_IMGAE not set yet !"
        exit
    fi
    if [[ ${ROCKETMQ_CONSOLE_IMAGE} == "" ]] ; then
        echo "env ROCKETMQ_SERVER_IMGAE not set yet !"
        exit
    fi
    if [[ ${DOCKER_ROCKETMQ_ROOT} == "" ]] ; then
        echo "env ROCKETMQ_SERVER_IMGAE not set yet !"
        exit
    fi

    # 2 检查镜像是否存在，不存在则拉取镜像
    for image in ${ROCKETMQ_SERVER_IMAGE} ${ROCKETMQ_BROKER_IMAGE} ${ROCKETMQ_CONSOLE_IMAGE} ; do
        image_infos=(${image//:/ })
        image_detail=`docker images | grep ${image_infos[0]} | grep ${image_infos[1]}`
        if [[ ${image_detail} == "" ]] ; then
            echo "docker pull ${image}"
            docker pull ${image}
        fi
    done

    # 3 创建映射目录
    if [[ $1 == "simple" ]] ; then
        server_logs_dir="${DOCKER_ROCKETMQ_ROOT}/rmqnamesrv/logs"
        server_store_dir="${DOCKER_ROCKETMQ_ROOT}/rmqnamesrv/store"
        broker_logs_dir="${DOCKER_ROCKETMQ_ROOT}/rmqbroker/logs"
        broker_store_dir="${DOCKER_ROCKETMQ_ROOT}/rmqbroker/store"
        for dir in ${server_logs_dir} ${server_store_dir} ${broker_logs_dir} ${broker_store_dir} ; do
            if [[ ! -d ${dir} ]]; then
                echo "mkdir -p ${dir}"
                mkdir -p ${dir}
            fi
        done
    elif [[ $1 == "2m2s" ]] ; then
        server1_logs_dir="${DOCKER_ROCKETMQ_ROOT}/rmqnamesrv1/logs"
        server1_store_dir="${DOCKER_ROCKETMQ_ROOT}/rmqnamesrv1/store"
        server2_logs_dir="${DOCKER_ROCKETMQ_ROOT}/rmqnamesrv2/logs"
        server2_store_dir="${DOCKER_ROCKETMQ_ROOT}/rmqnamesrv2/store"
        broker1m_logs_dir="${DOCKER_ROCKETMQ_ROOT}/rmqbroker1m/logs"
        broker1m_store_dir="${DOCKER_ROCKETMQ_ROOT}/rmqbroker1m/store"
        broker1s_logs_dir="${DOCKER_ROCKETMQ_ROOT}/rmqbroker1s/logs"
        broker1s_store_dir="${DOCKER_ROCKETMQ_ROOT}/rmqbroker1s/store"
        broker2m_logs_dir="${DOCKER_ROCKETMQ_ROOT}/rmqbroker2m/logs"
        broker2m_store_dir="${DOCKER_ROCKETMQ_ROOT}/rmqbroker2m/store"
        broker2s_logs_dir="${DOCKER_ROCKETMQ_ROOT}/rmqbroker2s/logs"
        broker2s_store_dir="${DOCKER_ROCKETMQ_ROOT}/rmqbroker2s/store"
        dirs=(${server1_logs_dir} ${server1_store_dir} ${server2_logs_dir} ${server2_store_dir} \
            ${broker1m_logs_dir} ${broker1m_store_dir} ${broker1s_logs_dir} ${broker1s_store_dir} \
            ${broker2m_logs_dir} ${broker2m_store_dir} ${broker2s_logs_dir} ${broker2s_store_dir})
        for dir in ${dirs[@]} ; do
            if [[ ! -d ${dir} ]]; then
                echo "mkdir -p ${dir}"
                mkdir -p ${dir}
            fi
        done
    fi
}

function container_up() {
    # 1 使用docker-compose启动docker集群
    echo ">>>>>>> rocketmq docker cluster start ..."
    if [[ $1 == "simple" ]] ; then
        docker-compose -f docker-compose-simple.yml up -d
    elif [[ $1 == "2m2s" ]] ; then
        docker-compose -f docker-compose-2m2s.yml up -d
    fi
    echo ">>>>>>> rocketmq docker cluster start done"
}

read -p "Please choose cluster mode (simple or 2m2s): " CLUSTER_MODE

if [[ ${CLUSTER_MODE} == "simple" || ${CLUSTER_MODE} == "2m2s" ]] ; then
    #加载配置项到环境变量
    source .env
    prepare ${CLUSTER_MODE}
    container_up ${CLUSTER_MODE}
else
    echo "Not support cluster mode !!!"
fi
