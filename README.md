<h1 align=center style="max-width: 100%;">
Just DDA it!
</h1>
<p align=center> SNS 기반 자격증+스터디 모바일 웹 어플리케이션 </p>

<p align=center style="line-height: 2;">
<img src="https://img.shields.io/badge/platform-Web-blue"/>
<img src="https://img.shields.io/badge/server-AWS-yellow"/>
<img src="https://img.shields.io/badge/database-mariaDB-blue"/>  
<img src="https://img.shields.io/badge/framework-spring%2C%20vue--cli-red"/>
<img src="https://img.shields.io/badge/language-Java%2C%20JavaScript-orange"/>


## overview

### 자격증

![20200825_205408](https://user-images.githubusercontent.com/60081199/91171328-453a4e80-e715-11ea-817e-e5b9b7058590.png)



+ 유저 정보 기반 자격증 추천 
+ 자격증 관련 정보 제공  
+ 리뷰  



### 스터디룸

![20200825_205420](https://user-images.githubusercontent.com/60081199/91171332-466b7b80-e715-11ea-8765-312d477b52a1.png)



- 스터디룸 개설 및 참여 기능 
- 일정 추가 및 공유 
- 공부 일지(이미지, 텍스트)

  - 댓글 
  - 좋아요 
  - 24시간 동안 가장 좋아요 많이 받은 유저 순위 제공



### 마이스터디

![20200825_205430](https://user-images.githubusercontent.com/60081199/91171321-42d7f480-e715-11ea-92de-ca85e99a5d49.png)

- 일정&스터디&인증샷 모아보기
- 투두리스트
- 준비예정, 준비중, 취득한 자격증 관리 



### 유저 관련 

+ 회원가입
  + 이메일 인증
+ 로그인
+ 비밀번호찾기
  + 이메일 인증
+ 회원탈퇴
+ 팔로우 기능



## Stack & Environment

```
 OS : Windows 10(develop), Ubuntu 18.04.1 LTS (deploy)
 IDE : Visual Stuid Code, Spring Tool Suite 3, IntelliJ
 DB : MariaDB
 Language : Java 1.8 , JavaScript
 Browser : Chrome / 아래 참고
 Framework : Spring, Vue, JPA
 Etc : Docker, Nginx, Jenkins, Sonarqube(pmd,checkStyle,findBugs)
 api : 공공데이터 포털 국가기술자격, 한국대학교육협의회_학교별학과현황(폐과포함) api 
```

 ##### [지원 브라우저](https://kangax.github.io/compat-table/es6) 

## Execution

- Local에서 실행 시, sdevelop 브랜치의 코드로 진행하세요.

- Docker가 없을 경우 -> [도커 설치](https://www.docker.com/get-started)  



### DB

**cmd**창이나 **terminal**창을 켠뒤, 다음 명령어 입력. 
만약, 실행환경이 **linux** 환경이라면 아래 명령어를 입력하기 전에 `sudo su` 명령어를 입력한다.

```
> docker run --name {DB 컨테이너 이름} -p 3306:3306 -e MYSQL_ROOT_PASSWORD={비밀번호} -d mariadb    # mariadb서버 생성
> docker exec -it maria-db mysql -u root --password={비밀번호}                                     # db서버 접속
```

그러면 DB에 접속이 된다. 
이제 다음 명령어를 입력해 skeleton이라는 데이터베이스를 생성한다.

```
MariaDB [(none)] > CREATE DATABASE SKELETON    # database 생성
MariaDB [(none)] > QUIT    # DB 빠져나오기
```

JPA를 통해 테이블을 생성할 것이기 때문에 DB는 추후 자격증 정보를 넣을 때 까지 신경쓸 필요가 없다.



### Backend

src/main/resources/application.properties에 들어간다. 
아래 내용만 수정한다. (안에 있는 정보 악용 금지!)  

```
(생략)
spring.datasource.url=jdbc:mysql://{DB 서버 주소}:3306/skeleton?(~이하 생략~)
spring.datasource.username=root
spring.datasource.password={DB 비밀번호}
(생략)
```

그리고 IDE에서 src/main/java/com/ssafy/study/**StudyApplication**을 Run 시킨다. ~~끝이다~~  

STS나 IntelliJ와 같은 IDE를 사용할 것을 추천하나, 피치 못할 사정으로 CLI방식으로 해야한다면...😭 
application.properties를 똑같은 형식으로 수정한 뒤, backend 디렉토리에서 command 창을 연다.

```bash
> mvn clean install   # !! mvn이 없으면 설치해야 함.
```



위 명령어를 통해 빌드를 한 뒤, 직접 웹 서버를 사용해 jar파일을 실행해야 한다.
docker를 사용하는 사람을 위해 Dockerfile을 만들어 놨으니(~~사실 내가 쓰다가 지우기 귀찮아서 놔둠...~~) docker를 사용한다면 다음과 같은 방법을 사용해도 된다.

```bash
> docker build -t {이미지 이름} -f Dockerfile ./
> docker run --name {서버 container 이름} -p 8080:8080 -d {이미지 이름}
```

이렇게 하면 8080포트로 백엔드 서버를 실행 시킬 수 있다.
##### 참고 [도커 빌드 관련](https://docs.docker.com/engine/reference/commandline/build/)




### Frontend

console창에 npm 명령어를 통해 서버를 실행한다.

```bash
> npm install
> npm run serve
```



### DB에 데이터 넣기
정상적인 서비스를 사용하기 위해서 자격증 정보를 DB에 넣어야한다. 
다음에 나올 내용들은 CLI환경에서 데이터를 넣는 방법으로, 조금 복잡해 나름 최대한 친절하게 설명해보겠다. GUI환경이라면 구글링을 통해 보다 더 간단하게 할 수 있다.
우선 아래 2개의 csv파일을 다운 받는다.  

##### [자격증1](https://lab.ssafy.com/s03-webmobile2-sub3/s03p13a102/blob/master/forREADME/licenses_for_db.csv)
##### [자격증2](https://lab.ssafy.com/s03-webmobile2-sub3/s03p13a102/blob/master/forREADME/field_info_all_output_for_db.csv)
DB서버를 배포하고 있는 환경에 다운을 받을 수 있으면 좋으나 CLI 환경에서 `wget`이나 `curl` 과 같은 command가 gitlab의 인증 문제 때문인지 실행이 되지 않는다.
그래서 이를 해결 할 두가지 방법을 간단히 설명하겠다.  

1. 배포 환경에서 git clone을 사용

   - git repository에 접근이 가능한 gitlab 계정이 필요하다. (아마 이 README.md 파일을 보고있다면 가능할 것이다.)
   - [git 일부 디렉토리만 clone](https://eventhorizon.tistory.com/20)하는 방식을 통해 해당 파일을 가지고 오면 된다. (전체를 clone해도 무방하다.)
   - 배포 환경에 git이 설치 되어 있어야 한다.

2. scp를 통해 해당 파일만 넘겨주기
      - 배포 환경에 22번 포트가 열려 있어야 하고, pem 키가 필요하다. ip 자체가 차단이 되어있다면 [iptable](https://sata.kr/entry/IPTables-2%ED%8A%B9%EC%A0%95-%EC%95%84%EC%9D%B4%ED%94%BC%EC%9D%98-%EC%B0%A8%EB%8B%A8%EA%B3%BC-%ED%97%88%EC%9A%A9-INPUT)을 사용해서 접속하려는 로컬의 ip를 허용해야한다.
      - 접근 하려는 배포 환경의 디렉토리에 접근 권한을 따로 설정해 주어야 한다.
      - 배포 환경의 디렉토리에 접근 권환을 주어야한다. 배포 환경에서 `chmod {디렉토리} 755`를 한다.
      - 현재 환경에서 `scp -i {pem 키} {자격증.csv} {배포환경 이름}:{디렉토리}` 명령어를 통해 csv파일을 옮길 수 있다.



이제 DB 서버를 배포하고 있는 환경에 csv 파일을 가지고 왔다. 하지만 여전히 중요한 문제가 남았다. 
설명을 따라 설치를 했다면 DB서버가 돌아가고 있는 환경은 docker의 container 위다. 이 csv 파일들을 컨테이너 환경으로 다시 옮겨야 하는 작업이 남았다.
만약 docker를 사용하지 않고 Apache나 다른 서버를 이용해 DB서버를 배포하고 있다면 다음 올 내용들은 생략해도 된다. 
!!만약, linux 서버에서 command를 실행한다면 `sudo su` 명령어를 사전에 입력해주어야한다. 
우선, MariaDB 서버가 돌아가는 container 안에 csv 파일을 넣을 디렉토리를 먼저 만들어주자. 
Maria DB 서버에서 `~/` 경로가 `/home/mysql/`이다. 따라서, 우리는 `/home/mysql/csv` 안에 이 csv 파일들을 넣으려고 한다.  

```bash
> docker exec -it {DB container 이름} /bin/bash   #DB 컨테이너의 shell(bash)에 접속을 할 건데 가상 tty를 통해 접속하는 명령어다.
```

이렇게 하면 DB container에 접속을 했다. 기본적으로 /home 디렉토리는 있으므로 이후 디렉토리만 생성한다.
```bash
root@{container id} > mkdir /home/mysql
root@{container id} > mkdir /home/mysql/csv
root@{container id} > exit
```

디렉토리를 생성했으니 해당 폴더에 csv 파일들을 옮겨보자  

```bash
> docker container cp {csv파일} {DB container 이름}:/home/mysql/csv   #두 개의 csv파일에 대해 각각 실행해준다. * 폴더를 넣어도 된다.
```

이제 csv 파일을 db에서 import를 해야한다. 우선 db 서버에 접속을 해보자.
```bash
> docker exec -it {DB container 이름} mysql -u root -p{비밀번호}      # -p{비밀번호}는 붙여써야한다.
```



DB서버에 접속을 했다. **참고로, 앞으로의 명령을 실행하기 위해서는 백엔드 서버를 한번이라도 실행을 시켜서 JPA를 통해 테이블이 생성된 상태여야 한다.**

```
MariaDB [(none)] > USE SKELETON
MariaDB [(skeleton)] > LOAD DATA INFILE '~/csv/field_info_all_output_for_db.csv'
                     > IGNORE
                     > INTO TABLE license_detail
                     > FIELDS TERMINATED BY '|'  
                     > ENCLOSED BY '"'  
                     > LINES TERMINATED BY '\n' 
                     > IGNORE 1 ROWS 
                     > (id, career, english_name, history, implementation_name, institute_name, license_name, field, field_category, license_series, summary, trend);
MariaDB [(skeleton)] > LOAD DATA INFILE '~/csv/licenses_for_db.csv'
                     > INTO TABLE licenses  
                     > FIELDS TERMINATED BY ','  
                     > ENCLOSED BY '"'  
                     > LINES TERMINATED BY '\n'  
                     > IGNORE 1 ROWS 
                     > (id,license_series,license_series_name,license_code, license_name, ncs_category_code1, ncs_category_name1,  ncs_category_code2,  ncs_category_name2);
```

이렇게 하면 DB 서버도 모두 준비가 되었다.



## Web Infrastructure
 ![Infrastructure](./forREADME/web%EA%B5%AC%EC%A1%B0.png)

## 간단 설명
aws ec2 서버 위에 docker를 설치 한 후, docker container 위에 3개의 nginx와 jenkins, sonarqube, MariaDB 총 6개의 컨테이너가 올라가있다. 
각각의 컨테이너에 대해 가볍게 알아보자.

### Jenkins
Jenkins는 CI(Continuous Integration)툴로 git 저장소(gitlab)에서 push나 merge와 같은 이벤트가 일어났을 때 webhook을 통해 jenkins에 전달이 되고 **자동 통합 및 빌드**가 일어난다. 
보다 자세한 내용은 [CI/CD](#cicd)쪽에서 다루겠다.  

##### [Jenkins sever](http://i3a102.p.ssafy.io:8090) 
id : `visitor`  , password : `s03p13a102`
### SonarQube
SonarQube에 있는 pmd, checkStyle, findBugs는 정적 분석 툴로 플러그인 형태로 SonarQube 사이트에 올라가있다. 
Jenkins에서 빌드 성공 시 코드를 정적 분석해주고 결과물을 제공한다.  

##### [SonarQube sever](http://i3a102.p.ssafy.io:8070/dashboard?id=ssafyProject) 
### nginx
3개의 nginx 위에는 프론트엔드 서버, 프록시 서버, 백엔드 서버가 올라가있다.
백엔드 서버는 하나의 nginx 컨테이너에 두 개의 Springboot jar가 다른 포트를 가지고 있어 서비스를 중지하지 않고 배포할 수 있다. 따라서, 프론트 서버에서 백엔드 서버로 요청을 보낼 때 어떤 백엔드 서버의 port가 연결되어 있는지 알 수 없다.  
이 문제를 해결하기 위해 proxy server를 두어 프론트 서버에서 8080포트로 요청을 보내면 proxy 서버가 8080포트로 오는 요청을 8081이 연결 되어 있으면 8081로, 8082가 연결 되어 있으면 8082로 요청을 보낸다.



## CI/CD Flow 
![CI/CD](./forREADME/CICD.png)

CI는 jenkins를 사용하였고 SonarQube와 연동을 하여 코드 정적 분석 기능을 추가하였다.
CD는 nginx를 통해 무중단 배포 서비스를 구현하였다.

### Flow Description <a name="cicd"></a>
로컬에서 코드를 작성 후 master 브랜치에 push를 하게되면 jenkins에 webhook이 날아가고 jenkins 서버에서 git으로부터 repository을 가지고 온다. 
git에서 정상적으로 가지고오면 jenkins에서 SonarQube 서버에 코드를 보내 검사를 요청한다. 
SonarQube에서 설치된 plugin에 따라 코드의 버그나 취약점 등 코드 분석을 실시한다.  

```
## SonarQube Plug-in  
PMD : 응용 프로그램에서 발견된 문제를 보고하는 오픈 소스 정적 Java 소스 코드 분석기. 문법적으로 오류 가능성이 높은 항목들을 체크해서 알려줌.  
CheckStyle : 코딩 스타일 규칙을 정의하여 체크해줌.  
FindBugs : 정적 분석 제공 툴. 기본적으로 발생할 수 있는 결함을 확인하고 Report해줌.  
```
SonarQube에 통과 기준을 설정해 분석 결과가 기준을 넘기면 **Pass**와 함께 결과를 jenkins에 보내준다. 
Jenkins는 성공 결과를 받으면 코드를 다음 script를 통해 빌드 한다.  

```
Backend는 `mvn clean install`
Frontend는 `npm install` 후, `npm run build`
```
빌드가 완료되면  빌드를 통해 나온 실행 파일 및 디렉토리들을 scp를 통해 aws ec2서버로 보낸다. 
동시에 aws ec2서버에 ssh로 접속해 shell script 파일을 실행시켜 각각 서버를 실행시킨다.

```
Frontend는 docker container를 restart하는 방법을 통해 배포한다.
Backend는 여러 yml 파일들을 통해 8081포트와 8082포트 중 사용되지 않은 포트를 선택하여 구동시키고 다른 포트로 배포되어있는 서버를 종료시킨다.
```



## Develop Rules

### branch

```
master -> develop -> frontend / backend
```

```
branch name : FE/BE-기능
```

### merge

```
merge request: [FE]/[BE] 기능 
```

```
merge 후 브랜치 지우기
```

### commit 

```
commit message : branch이름 이슈번호 내용 
```



## 팀원 및 역할

| 팀원         | 역할                            |
| ------------ | ------------------------------- |
| 김진혁(팀장) | 자격증탭 FE, gitlab과 jira 관리 |
| 곽세경       | 스터디탭 FE, ui & ppt 디자인    |
| 허예슬       | 마이스터디&사용자인증 FE, ucc   |
| 이유진       | BE, data modeling               |
| 박형민       | BE, CI&CD, aws 배포             |



## 프로젝트를 끝으로 남은 것  

1. git을 이용한 협업 경험

   프로젝트를 통해 익힌 git 관련 내용을 간단하게 상기하며 정리해보았다. 

   ```bash
   # 브랜치 생성
   $ git branch {branch name}
   # 로컬 브랜치 목록 조회
   $ git branch
   # 브랜치 목록 조회 + 중앙 저장소 브랜치
   $ git branch -a
   # 해당 브랜치로 전환
   $ git checkout {branch name}
   # 브랜치 삭제
   $ git branch -d {branch name} 
   ```

   ```bash
   # 작업 순서
   # 1. 브랜치 생성(상위 브랜치에서)
   $ git branch test
   # 2. 브랜치로 이동
   $ git checkout test
   # 3. 작업
   # 4. 브랜치에 전체 파일 추가 및 커밋
   $ git add .
   $ git commit -m "commit message"
   # 5. 원격 저장소에 push => 원격 저장소에 test 브랜치 생성 
   $ git push -u origin test 
   # 6. 작업 
   # 7. 원격 저장소 pull : push 이전에 원격 저장소의 최신 커밋 이력을 포함시켜야한다
   $ git pull origin master
   # 8. 원격 저장소에 push => 현재 브랜치를 원격 저장소 test에 동기화! 
   $ git push origin test 
   # 9. 병합 요청 (test -> master) : 웹에서 진행
   ```

   - `origin` : 원격 저장소 복제할 때 자동 생성된 원격 저장소의 별칭 

   - `-u` : 새로운 브랜치와 똑같은 이름으로 원격 저장소의 브랜치를 추가할 때 사용

   

2. FE & BE 협업과 커뮤니케이션

   이전 프로젝트와 달리 Frontend와 Backend를 분리하여 진행하였고, 나는 FE만 맡아서 했는데 이 둘이 소통하는 과정이 원활하지 못했던 것 같다. method, params, response, data type 등을 작성한 표를 이용해 소통해보고자 하였으나 잘 안됐고 BE를 맡은 친구들이 상상으로 코드를 작성했다는 이야기를 해줬을 때 뭔가 잘못됐음을 느꼈다. 원활할 커뮤니케이션에 대한 고민이 필요할 것 같다. 

   또, 다음부터는 코드의 가독성을 더더 고려하고싶다!



3. 애자일 개발 프로세스 경험

   주마다 공개되는 명세에 따라 스프린트 개발 주기가 정해졌다. jira를 이용해 완성해야 할 기능들의 우선 순위를 정하고, 매일 두 번씩 스크럼 회의를 진행하였다. 자신의 분야에만 국한되지 않고 전체적인 프로젝트의 흐름을 따라갈 수 있다는 점에서 스크럼 회의의 중요성이 느껴졌다. 다만 맡은 업무를 완료하기까지 걸리는 시간에 대한 예측이 미숙하여 계획이 틀어지는 경우가 많았고, 후반으로 갈수록 jira에 백로그를 완성하는 업무의 필요성을 느끼지 못했다. 

   

4. 아키텍처 설계의 중요성 

   결과물은 결국 기초 공사에서 나오는 것 같다. 다음프로젝트에서는 아키텍처 설계에 집중해보고싶다.  

    

5. 최적화 문제

   배포까지 완료한 후 완성한 웹 사이트의 통신 속도가 느리다는 것을 알게되었다. 개발하는 과정에서 성능 최적화에 대한 부분은 전혀 고려하지 않았다는 것을 깨달았다. 성능 측정 및 최적화를 위한 개선이 필요할 것 같다. 



6. 배움..

   구글링 -> 복붙을 한 코드에 대해서는 머리에 남는게 없는 기분이 든다.  다음부터는 느리더라도 원리를 이해하면서 코드를 작성하겠다. 또 배포나 CI/CD를 직접 해보고싶다. 

