# devops-demo

Docker image: https://hub.docker.com/repository/docker/asandy1023/devops-demo

# DevOps-demo 發佈系統流程

*DevOps-demo 發佈系統,主要是利用GitLab 提供的CI機制,來實現在程式發生提交或合併等事件時自動觸發預設的CI/CD流程。*

* CI階段主要包括基本的程式編譯、建構和打包,並將打包好的應用
Docker 映像檔發佈至Docker Hub中

* CD階段則是從Docker Hub拉取Docker 映像檔應用,並根據設定的 CD 流程將應用發佈至指定的Kubernetes 叢集中
* DevOps 發佈系統主要由GitLab、映像檔倉庫及Kubernetes 組成。其中,GitLab 主要承擔程式版本管理,以及CI/CD流程的定義和觸發;Docker Hub 負責Docker 映像檔的儲存和分發,Kubernetes 應用容器執行的基礎架構環境


## 部署 GitLab 程式倉庫

### Ubantu部署 GitLab-ce


* update本地套件
```
docker pull ubuntu
apt-get update
apt-get install sudo
sudo apt update
sudo apt upgrade
sudo apt-get install -y curl openssh-server ca-certificates tzdata perl
```

* 安裝Postfix 以發送郵件通知
```
sudo apt-get install -y postfix
```


* 下載GitLab 安裝
```
cd /tmp/
curl https://packages.gitlab.com/install/repositories/gitlab/gitlab-ee/script.deb.sh | sudo bash
sudo apt install gitlab-ce
```
* 編輯設定檔
```
sudo apt install nano
nano /etc/gitlab/gitlab.rb
```
>"external_url"設定專案設定為實際的ip

* 執行重新設定 GitLab 
```
sudo gitlab-ctl reconfigure
```

* 重新啟動 GitLab 服務
```
sudo gitlab-ctl restart
```



## 部署 GitLab Runner

*GitLab Runner 建構任務部署 GitLab Runner 元件*

* 在Linux伺服器上部署 GitLab Runner,命令如下: +透過道命令增加映像檔倉庫
```
docker run -d --name gitlab-runner --restart always 
-v /srv/gitlab-runner/config:/etc/gitlab-runner 
-v /var/run/docker.sock:/var/run/docker.sock 
gitlab/gitlab-runner:latest
```
* 啟動 GitLab Runner
```
sudo apt-get install --reinstall systemd
sudo systemctl enable gitlab-runner
```
* 自啟動GitLab Runner
```
systemctl start gitlab-runner
```
* 查看 GitLab Runner 的執行狀態
```
systemctl status gitlab-runner
```
* 註冊到CI/CD server
```
sudo gitlab-runner register
```
>enter url:CI/CD server ip
>
>token:CI/CD token
>
>executor:shell or kubernetes



* install Maven 及 Docker 環境
```
apt install maven
sudo apt-get update
sudo apt-get install docker-ce docker-ce-cli containerd.io docker-compose-plugin
```



## Kubernetes setup & install

Kubernetes 是由 Google 與RedHat 公司共同主導的開放原始碼容器編排專案,它起源於 Google 公司的 Borg 系統,所以它在超大規模叢集管理方面的經驗明顯優於其他容器編排技術

在功能上。Kubernetes 是一種綜合的、以容器建構分散式系統為基礎的 基礎架構環境。它不僅能夠實現基本的拉取映像檔和執行容器,還可以 提供路由閘道、水平擴充、監控、備份、災難恢復等一系列執行維護能力。

更重要的是,Kubernetes 可以按照使用者的意願和整個系統的規則。 高度自動化地處理容器之間的各種關係實現「編排」能力。




#### 使用 Kubeadm,簡單快速部署Kubernetes 叢集

* 安裝 Kubeadm 及Docker 環境

這裡省略了一些流程，實際還是依照官方文件來做較保險
https://kubernetes.io/docs/setup/production-environment/tools/kubeadm/install-kubeadm/

```
sudo apt-get update
sudo apt-get install docker-ce docker-ce-cli containerd.io docker-compose-plugin
```

* 更新apt 資源列表
```
apt-get update Hit:1 http://cn.archive.ubuntu.com/ubuntu focal InRelease
Hit:2 http://cn.archive.ubuntu.com/ubuntu focal-updates InRelease
Hit:3 http://cn.archive.ubuntu.com/ubuntu focal-backports InRelease 
Hit:4 http://cn.archive.ubuntu.com/ubuntu focal-security InRelease) 
Get:5 https://packages.cloud.google.com/apt kubernetes-xenial InRelease
```

* 修改 Docker 作業系統限制
```
cd /etc/default/grub 
GRUB_CMDLINE_LINUX="cgroup_enable=memory swapaccount=1"
```
* 重新啟動伺服器
```
update-grub
reboot
```
* 禁用虛擬記憶體
```
/opt/kubernetes-config# swapoff -a
```

* 啟動Docker 服務
```
systemctl enable dooker.service
```




### 部署 Master 節點
```
cd /opt/kubernetes-config
touch kubeadm.yaml
```

* kubeadm.yaml:
```
apiVersion: kubeadm.k8s.io/vlbeta2
kind: ClusterConfiguration
controllerManager:
extraArgs:
    horizontal-pod-autoscaler-use-rest-clients: "true"
    horizontal-pod-autoscaler-sync-period:"10s"
    node-monitor-grace-period: "10s"

apiServer:
    extraArgs:
        runtime-config: "api/all=true" 
kubernetesVersion: "vl.18.1"
```

* see the Master nodes images
```
docker images
```

* 檢查Pod的狀態
```
/opt/kubernetes-config kubectl get pods -n kube-system
```



### 部署 Kubernetes 的 Worker 節點

```
kubeadm join <MasterIP> --token <Mastertoken> 
--discovery-token-ca-cert-hash sha256:<Masterdiscoverytoken> -v=5
```

* 建立設定目錄
```
mkdir -p $HOME/.kube
scp root@10.211.55.6:$HOME/.kube/config $HOME/.kube/
sudo chown $(id -u):S (id -g) SHOME/.kube/config
```
* 就可以在 Worker 和 Master 節點執行節點狀態查看
```
kubectl get nodes

>kubenetesnode02
>
>kubernetesnode01 ROLES:master
```

* 準備 GitLab CI/CD server Kubernetes 環境
```
snap install kubectl -classic
kubectl version -client
```

* setup "/kube/config" 設定檔中 Kubernetes 叢集的造訪網址及存取證書等資訊

```
    apiVersion: v1
    
    clusters:
    
    - cluster: 
        certificate-authority-data:  
        server: <IP> 
    name: kubernetes
    
    contexts:
    
        - context:
    
            cluster: kubernetes
            user: kubernetes-admin 
        name: kubernetes-admin@kubernetes
    current-context: kubernetes-admin@kubernetes
    kind: Config
    preferences: ()
    users:
    - name: kubernetes-admin
        user: client-certificate-data: XXXX
        client-key-data: XXXX
```




### 將微服務應用自動發佈到 Kubernetes 叢集中 
```
docker run name consul -p 8500:8500 -v /tmp/consul/conf/:/consul/conf/
-v/tmp/consul/data/:/consul/data/ -d consul
```

* .gitlab-ci.yml, deploy階段將自動發佈到 Kubernete
```
deploy-test:
  stage: deploy
```


## 連接 Kubernetes 叢集, 查看Pod 執行

```
kubectl get po -n asandy1023 -o wide
```
its show these:

NAME  READY STATUS  RESTARTS  AGE IP NODE NOMINATED NODE  READINESS GATES

**devops-demo 1/1 Running 0   0h10m   10.32.0.6   kubernetes <none> <none>**

