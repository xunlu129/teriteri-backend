-- MySQL dump 10.13  Distrib 8.0.31, for Win64 (x86_64)
--
-- Host: 47.113.150.190    Database: teriteri
-- ------------------------------------------------------
-- Server version	5.7.43-log

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `category`
--

DROP TABLE IF EXISTS `category`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `category` (
  `mc_id` varchar(20) NOT NULL COMMENT '主分区ID',
  `sc_id` varchar(20) NOT NULL COMMENT '子分区ID',
  `mc_name` varchar(20) NOT NULL COMMENT '主分区名称',
  `sc_name` varchar(20) NOT NULL COMMENT '子分区名称',
  `descr` varchar(200) DEFAULT NULL COMMENT '描述',
  `rcm_tag` varchar(500) DEFAULT NULL COMMENT '推荐标签',
  PRIMARY KEY (`mc_id`,`sc_id`),
  KEY `mc_id` (`mc_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='分区表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `category`
--

LOCK TABLES `category` WRITE;
/*!40000 ALTER TABLE `category` DISABLE KEYS */;
INSERT INTO `category` VALUES ('animal','animal_composite','动物圈','动物综合','收录除上述子分区外，其余动物相关视频以及非动物主体或多个动物主体的动物相关延伸内容。如动物资讯、动物趣闻、动物知识等','昆虫\n动物\n水族馆\n汪星人\n宠物\n国宝\n搞笑动物\n动物园\n海洋\n猫咪\n'),('animal','cat','动物圈','喵星人','与猫相关的视频，包括但不限于猫咪日常、猫咪喂养、猫咪知识、猫咪剧场、猫咪救助、猫咪娱乐相关的内容','小猫咪\n流浪猫\n小奶猫\n美短\n宠物\n银渐层\n猫猫\n日常\n萌宠\n睡觉'),('animal','dog','动物圈','汪星人','与狗相关的视频，包括但不限于狗狗日常、狗狗喂养、狗狗知识、狗狗剧场、狗狗救助、狗狗娱乐相关的内容','中华田园犬\n博美\n拉布拉多\n土狗\n宠物\n狗狗\n汪星人\n柴犬\n金毛\n吃货'),('animal','reptiles','动物圈','小宠异宠','非猫、狗的宠物。包括但不限于水族、爬宠、鸟类、鼠、兔等内容','乌龟\n金丝熊\n兔兔\n荷兰猪\n异宠\n宠物兔\n中华草龟\n爬宠\n可爱\n水族馆'),('animal','second_edition','动物圈','动物二创','以动物素材为主的包括但不限于配音、剪辑、解说、reaction的再创作内容','动物世界\n可爱\n汪星人\n搞笑动物\n狮子\n治愈系\n狗狗\n动物救援\n动物圈\n自然'),('animal','wild_animal','动物圈','野生动物','与野生动物相关的视频，包括但不限于狮子、老虎、狼、大熊猫等动物内容','搞笑\n熊猫\n萌宠\n熊猫基地\n可爱\n熊猫宝宝\n国宝\n动物园\n大象\n猛兽'),('anime','finish','番剧','完结动画','已完结的动画番剧合集','孤独摇滚\n经典\n剧场版\nTV动画\nJOJO的奇妙冒险\n名场面\n动漫\n热血\n治愈\n二次元'),('anime','information','番剧','资讯','以动画/轻小说/漫画/杂志为主的资讯内容，PV/CM/特报/冒头/映像/预告','新番预告\nF宅字幕组\n二次元\n第二季\n十月\n漫改\n轻小说\n恋爱\n咒术回战\n综合'),('anime','offical','番剧','官方延伸','以动画番剧及声优为主的EVENT/生放送/DRAMA/RADIO/LIVE/特典/冒头等','莲之空\n生放送\n青山吉能\n广播剧\n动漫\n可爱\n佐仓绫音\n爱美\nDRAMA\nラジオ'),('anime','serial','番剧','连载动画','当季连载的动画番剧','OVA·OAD\n剧场版\nTV动画\n日本动画\nMADHOUSE'),('car','life','汽车','汽车生活','和汽车等交通工具相关的—切泛化内容的视频','公交\n旅行\n宝马\n公交POV\n第一视角\n前面展望\n驾考\n比亚迪\n驾驶技能\n学车'),('car','modifiedvehicle','汽车','改装玩车','汽车文化、玩车改装为代表的相关内容。包括但不限于痛车、汽车模型、改装车、自制车以及汽车创意玩法等衍生相关视频','维修\n丰田\nJDM\n车灯\n改装\n保时捷\n车模\n宝马\n赛车\n模型'),('car','motorcycle','汽车','摩托车','与摩托车相关的视频，包括但不限于摩托车骑行、试驾、装备测评、教学、赛事、剪辑等相关内容','汽车\n鬼火'),('car','newenergyvehicle','汽车','新能源车','新能源汽车相关内容，包括电动汽车、混合动力汽车等车型种类，包含不限于新车资讯、试驾体验、专业评测、技','理想\n新能源\n自驾游\n汽车文化\n体验\n小鹏\n新能源汽车\n比亚迪\n国产车\n蔚来'),('car','racing','汽车','赛车','一切以汽车运动为代表的车手、汽车赛事、赛道、赛车模拟器、赛车、卡丁车及赛车衍生品相关的视频','摩托车\n模型\n拉力赛\n拉力\n游戏\n竞速\n汉密尔顿\n纽北\n汽车文化\nF1赛车'),('car','strategy','汽车','购车攻略','新车、二手车测评试驾，购车推荐，交易避坑攻略等','吉利\n小姐姐\n捷途汽车\n新车\n新能源汽车\n丰田\n买车\n测评\n比亚迪\n奔驰'),('car','touringcar','汽车','房车','房车及营地相关内容，包括不限于产品介绍、驾驶体验、房车生活和房车旅行等内容','房车\n方舱房车\n房车展\n房车自驾\n皮卡房车\n房车旅行\n越野房车\n房车改装\n中正房车\n旅行车'),('cinephile','cinecism','影视','影视杂谈','影视评论、解说、吐槽、科普、配音等','短片\n原创\n影视剪辑\n剪辑\n搞笑'),('cinephile','montage','影视','影视剪辑','对影视素材进行剪辑再创作的视频','CP\n混剪\n经典电影\n美女\n短片\n长相思\n电视剧\n邓为\n美剧\n电影'),('cinephile','shortfilm','影视','短片','各种类型的短片，包括但不限于真人故事短片、微电影、学生作品、创意短片、励志短片、广告短片、摄影短片、纪实短片、科幻短片等','剪辑\n微电影\n故事\n悬疑\n原创\n爱情\n正能量\n搞笑\n摄影\n美女'),('cinephile','shortplay','影视','小剧场','单线或连续剧情，且有演绎成分的小剧场(短剧)内容','短剧\n电影剪辑\n小说推文\n经典电影\n动漫\n搞笑\n原创\n恋爱\n古装\n情感'),('cinephile','trailer_info','影视','预告·资讯','影视类相关资讯，预告，花絮等视频','杨紫\n电影\n爱情\n动作\n喜剧\n科幻\n影视\nCP\n于适'),('dance','china','舞蹈','国风舞蹈','收录国风向舞蹈内容，包括中国舞、民族民间舞、汉唐舞、国风爵士等','现场\n可爱\n红楼梦\n小姐姐\n翻跳\n现代舞\n舞剧\n国风\n原创编舞\n表演'),('dance','demo','舞蹈','舞蹈教程','动作分解，基础教程等具有教学意义的舞蹈视频','舞蹈练习\n舞蹈教程\n舞蹈教学\n热舞\n自用\n爵士舞\n舞蹈翻跳\n跳舞\n民族舞\n中国舞'),('dance','gestures','舞蹈','手势·网红舞','手势舞及网红流行舞蹈、短视频舞蹈等相关视频','女主播\n街舞\n搞笑\n诱惑\n韩舞\n可爱\n最新\n宅舞\n大长腿\n直播录像'),('dance','hiphop','舞蹈','街舞','收录街舞相关内容，包括赛事现场、舞室作品、个人翻跳、FREESTYLE等','小姐姐\n韩舞\n街舞\n现场\nUrban\n舞蹈教程\n编舞\n时尚\n热舞\n基础'),('dance','otaku','舞蹈','宅舞','与ACG相关的翻跳、原创舞蹈','翻跳\n随机舞蹈\n随机宅舞\n宅舞\n舞蹈翻跳\n诱惑\n热舞\ncos\n现场\n元气'),('dance','star','舞蹈','明星舞蹈','国内外明星发布的官方舞蹈及其翻跳内容','直播录像\n舞蹈教程'),('dance','three_d','舞蹈','舞蹈综合','收录无法定义到其他舞蹈子分区的舞蹈视频','穿搭\n舞蹈翻跳\n身材\n跳舞\n舞蹈教程\n宅舞\n搞笑\n大长腿\n诱惑\n秀场'),('douga','acgntalks','动画','动漫杂谈','ACGN文化圈杂谈内容','灰原哀\n动画短片\n热血'),('douga','garage_kit','动画','手办·模玩','手办模玩的测评、改造或其他衍生内容','潮玩\n高达模型\n假面骑士\n汽车模型\n积木\n模型\n手办\n模玩\n手办模玩\n原创'),('douga','mad','动画','MAD·AMV','具有—定创作度的动/静画二次创作视频','剪辑\n搞笑\n原神\n爱情\n鬼灭之刃\n动画短片\n可爱\n泪目\n音乐\n原创'),('douga','mmd','动画','MMD·3D','使用MMD和其他3D软件制作的视频','3D\n原神MMD\nVAM\n美女\n动画短片\n虚拟偶像\n原神\n原创\n米哈游\n3D动画'),('douga','other','动画','综合','以动画及相关内容为素材的创作','原创\n可爱\nVUP\n童年\nAMV\n国产动画\n二次元\n咒术回战\nMAD\n高燃'),('douga','tokusatsu','动画','特摄','特摄相关衍生视频','搞笑\n赛罗奥特曼\n热血\n布莱泽\n圆谷\n假面骑士\n童年\n特摄\n奥特曼\n铠甲勇士'),('douga','voice','动画','短片·手书·配音','追求个人特色和创意表达的动画短片及手书(绘)','原创\n原创动画\n动画短片\n手书\n二次元\n原神\n虚拟偶像\n加查俱乐部\n记录\n同人'),('ent','celebrity','娱乐','明星综合','娱乐圈动态、明星资讯相关','影视剪辑\n剪辑\n搞笑\n爱情\n自制'),('ent','fans','娱乐','粉丝创作','粉丝向创作视频','自制\n张泽禹\n高甜\n可爱\n混剪\n偶像\n韩国\n爱情\n帅哥\n安利向'),('ent','talker','娱乐','娱乐杂谈','娱乐人物解读、娱乐热点点评、娱乐行业分析','原创\n自制\n女神\n情感\n高甜\n明星\n周淑怡\n吐槽\n歌手\n爱情'),('ent','variety','娱乐','综艺','所有综艺相关，全部—手掌握!','爆笑\n石凯\n韩国综艺\n十个勤天\n相声\n蒲熠星\n马立奥\n韩国\n爱情\n娱乐'),('fashion','clothing','时尚','穿搭','穿搭风格、穿搭技巧的展示分享，涵盖衣服、鞋靴、箱包配件、配饰（帽子、钟表、珠宝首饰)等','JK制服\n鞋子\n丝袜\n首饰\n分享\n美妆\n美腿\n莆田鞋\n性感\n珠宝'),('fashion','cos','时尚','仿妆COS','对明星、影视剧、文学作品、动漫、番剧、绘画作品、游戏等人物角色妆容或整体妆造进行模仿、还原、展示、演绎的内容','化妆\n克拉拉\n美女\ncos\n女装\n可爱\n整活\n仿妆\n二次元\n玉足'),('fashion','makeup','时尚','美妆护肤','彩妆护肤、发型美甲、仿妆、美容美体、口腔、拍照技巧、颜值分析等变美相关内容分享或产品测评','日常\n护肤品\n化妆\n日常妆\n穿搭\n变美\n妆容\n男士发型\n彩妆\n发型'),('fashion','trend','时尚','时尚潮流','时尚街拍、时装周、时尚大片，时尚品牌、潮流等行业相关记录及知识科普','女神\n秀场\n走秀\n时尚\n机械表\n时装秀\n种草\n性感\n泳装\n品牌'),('food','detective','美食','美食侦探','包括但不限于探店、街边美食、饮食文化，发现特色地域美食、路边摊与热门网红食物等','烘焙\nVlog'),('food','make','美食','美食制作','包括但不限于料理制作教程，各种菜系、甜点、速食、饮料、小吃制作等','家常菜\n烘焙\n甜品\n生活记录\n简单\n吃货\n鸡蛋\n下饭菜\n一人食\n减肥'),('food','measurement','美食','美食测评','包括但不限于边吃边聊、测评推荐或吐槽各种美食等','街头美食\n糖果\n减肥\n美味\n开箱\n美食\n月饼\n试吃\n好吃\nVlog'),('food','record','美食','美食记录','记录—日三餐，美食vlog、料理、便当、饮品合集、美食小剧场等','记录\n厨艺\n美食制作\n解压\n生活\n火锅\n声控\n美食\n减肥\n咀嚼音'),('food','rural','美食','田园美食','包括但不限于多野美食、三农采摘、钓鱼赶海等','螃蟹\n乡村美食\n种植\n乡村\n田园美食\n搞笑\n海鲜\n户外\n美食教程\n吃货'),('game','board','游戏','桌游棋牌','桌游、棋牌、卡牌、聚会游戏等相关视频','三国杀OL\n麻将\n教程攻略\n炉石传说\n桌游棋牌\n围棋\n游戏王\n扑克\n日本麻将\n精彩集锦'),('game','esports','游戏','电子竞技','电子竞技游戏项目为主要内容的相关视频','CS:GO\n搞笑\n游戏\n原神\nAPEX\nFPS\n竞技游戏\n守望先锋\n第五人格\nCS'),('game','gmv','游戏','GMV','使用游戏内容或CG为素材制作的MV类型的视频','动作游戏\n混剪\n冒险游戏\nFPS\nMOBA\n高燃\n音乐\nGMV\n可爱\n沙雕'),('game','mobile','游戏','手机游戏','手机及平板设备平台上的游戏相关视频','原神周年庆\n二次元\n电子竞技\n暗区突围\n教程攻略\n星穹铁道\nMOBA\nFPS\n冒险游戏\n决斗场'),('game','mugen','游戏','Mugen','使用Mugen引擎制作或与Mugen相关的游戏视频','沙雕\n原创\n人物\n连招\n八神\n拳皇\n搞笑\n草雉京\n手操\n单机游戏'),('game','music','游戏','音游','通过配合音乐与节奏而进行的音乐类游戏视频','录屏\nFNF\n二次元\nAP\n哺斯快跑\n自制谱\n娱乐\n韵律源点\n冰与火之舞\n通关视频'),('game','online','游戏','网络游戏','多人在线游戏为主要内容的相关视频','娱乐\n游戏\n穿越火线\n吃鸡\n怀旧\n第—视角\n精彩集锦\n动作游戏\n搞笑\n直播录像'),('game','stand_alone','游戏','单机游戏','以单机或其联机模式为主要内容的相关视频','生存\nMOD\n实况解说\n娱乐\nFPS\n动作游戏\n塞尔达传说\n游戏解说\n二次元\n沙盒游戏'),('guochuang','chinese','国创','国产动画','国产连载动画，国产完结动画','动画\n童年回忆\n凹凸世界\n国漫\n时光代理人\n雾山五行\n混剪\n漫画解说\n可爱\n完美世界'),('guochuang','information','国创','资讯','原创国产动画、漫画的相关资讯、宣传节目等','励志\n广播剧\n搞笑\nCV\n国漫\n进出口\n预告\n原创动画\n原创\n古风'),('guochuang','motioncomic','国创','动态漫·广播剧','国产动态漫画、有声漫画、广播剧','有声小说\n纯爱\n有声漫画\n小说\n热血\n谷江山\n动漫\n有声书\n文荒推荐\n搞笑'),('guochuang','original','国创','国产原创相关','以国产动画、漫画、小说为素材的二次创作','搞笑\n网文—口气看完\n小说\n原耽\n古风\n宝藏小说\n完结\n有声漫画\n重生'),('guochuang','puppetry','国创','布袋戏','布袋戏以及相关剪辑节目','武侠\n爱国\n霹雳\n霹雳布袋戏\n二创\n古风\n中国\n霹雳天机\n配音\n混剪'),('information','global','资讯','环球','全球范围内发生的具有重大影响力的事件动态','联合国\n世卫组织\n俄乌冲突'),('information','hotspot','资讯','热点','全民关注的时政热门资讯','党中央\n新政策'),('information','multiple','资讯','综合','除上述领域外其它垂直领域的综合资讯','综合资讯\n环球报道'),('information','social','资讯','社会','日常生活的社会事件、社会问题、社会风貌的报道','淄博\n广交会'),('kichiku','course','鬼畜','教程演示','鬼畜相关的科普和教程演示','沙雕\ninm\n蔡徐坤\n抽象\n电棍\n淳平\nhomo\n我修院\n素材\n答辩'),('kichiku','guide','鬼畜','鬼畜调教','使用素材在音频、画面上做一定处理，达到与BGM具有一定同步感的视频','鬼畜小赏\n坤坤\nikun\n高能\n洗脑循环\n原神\n音MAD\n原创\n抽象\n自制'),('kichiku','mad','鬼畜','音MAD','使用素材音频进行—定的二次创作来达到还原原曲的非商业性质稿件','原神\n野兽先辈\n山泥若\n炫神\n蔡徐坤\n音MAD\n鬼畜\n创价\n娱乐\n东方'),('kichiku','manual_vocaloid','鬼畜','人力VOCALOID','将人物或者角色的无伴奏素材进行人工调音，使其就像VOCALOID—样歌唱的技术','秀才\n全明星\notto\n鬼畜翻唱\n兰亭序\nikun\n原神\n电子竟技\n鬼畜\n鸡你太美'),('kichiku','theatre','鬼畜','鬼畜剧场','使用素材进行人工剪辑编排的有剧情的视频','超人强\n鸡你太美\n沙雕\n秀才\n刘华强\n生活\n鬼畜素材\n高能\n搞笑\n鬼畜小赏'),('knowledge','business','知识','财经商业','财经/商业/经济金融/互联网等','创业\n金融\n数字货币\n互联网\n商业\n经济\n学习\n科普\n赚钱\n经验分享'),('knowledge','campus','知识','校园学习','学习方法及经验、课程教学、校园干货分享等','英语\n高三学习心得\n数学\n课程\n高中\n知识\n留学\n日语学习\n高中数学'),('knowledge','career','知识','职业职场','职场技能、职业分享、行业分析、求职规划等','考试\n自媒体\n分享\n—级建造师\n工作\n创业\n经验分享\n公务员考试\n找工作\n知识'),('knowledge','design','知识','设计创意','以设计美学或基于设计思维展开的知识视频','建模\nC4D\nPS\n学习\n品牌设计\n3D\n人工智能\n渲染\n平面设计\n视频教程'),('knowledge','humanity_history','知识','人文历史','人物/文学/历史/文化/奇闻/艺术等','古诗词\n哲学\n俄罗斯\n人生读书\n教育\n文化\n文学\n传统文化\n艺术'),('knowledge','science','知识','科学科普','以自然科学或基于自然科学思维展开的知识视频','学习\n化学\n中药\n日本\n涨知识\n经验分享\n天文\n实验\n历史\n健康科普'),('knowledge','skill','知识','野生技能协会','技能展示或技能教学分享类视频','学习\n经验分享\n学习心得'),('knowledge','social_science','知识','社科·法律·心理','法律/心理/社会学/观点输出类内容等','哲学\n学习\n孩子\n案件\n原创\n科普\n人文恋爱技巧\n读书\n追女生'),('life','daily','生活','日常','—般日常向的生活类视频','开箱\n助眠\n小视频\n夏天\n旅游\n正能量\n生活记录\n学习\n生活\n记录'),('life','funny','生活','搞笑','搞笑挑战、剪辑、表演、配音以及各类日常沙雕视频','娱乐\n幽默\n万恶之源\n搞笑\n记录\n原神\n吐槽\n新人\n直播\n原创'),('life','handmake','生活','手工','与手工艺、DIY、发明创造相关的视频，例如手工记录、四坑手作、毛毡粘土、手账文具、写字书法、模型、玩具、解压、传统手艺、非遗等','练字\n生活记录\n开箱\n原创\n流麻\n福袋\n艺术\n手写\n解压\n可爱'),('life','home','生活','家居房产','与买房、装修、居家生活相关的视频，如买房租房、装修改造、智能家居、园艺绿植、居家好物等','原创\n房价\n定制\n全屋定制\n楼市\n家装\n别墅\n家具\n好房推荐\n装修'),('life','painting','生活','绘画','与绘画、艺术、设计相关的视频，例如绘画记录、数字绘画、手绘、潮流艺术、创意作画、绘画教程、美术分享、动漫、插画等','新人\n插画\n画画\nFurry\n原创\n约稿\n记录手绘\n绘画\n指绘'),('life','parenting','生活','亲子','与萌娃、母婴、育儿相关的视频，包括但不限于萌娃日常、萌娃才艺、亲子互动、亲子教育、母婴经验分享、少儿用品分享等','情感\n娱乐\n记录\n带娃\n可爱\n玩具\n原创\n健康\n人类幼蕙\n高能'),('life','rurallife','生活','三农','与农业、农村、农民相关的视频，包括但不限于农村生活、户外打野、种植技术、养殖技术、三农资讯','生活记录\n记录\n学习'),('life','travel','生活','出行','旅行、户外、本地探店相关的视频，如旅行vlog、治愈系风景、城市景点攻路、自驾游、户外露营、演出看展等','生活记录\n记录\n学习'),('music','commentary','音乐','乐评盘点','该分区收录音乐资讯、音乐点评盘点、音乐故事等，包括但不限于音乐类新闻、盘点、点评、reaction、榜单、采访、幕后故事等。','歌声\n基础唱法\n流行\nrap\n音乐\n周杰伦\n动人\n经典\n音乐现场\n好听'),('music','cover','音乐','翻唱','对曲目的人声再演绎视频。','女声\n虚拟UP主\n吉他\n唱歌\n弹唱\n新人\n凌音阁\n治愈\nCOVER\n男声'),('music','live','音乐','音乐现场','该分区收录户外或专业演出场所公开进行音乐表演的实况视频，包括但不限于官方/个人拍摄的综艺节目、演唱会等音乐演出内容。','翻唱\n乐队\n张杰\n现场\n周杰伦\n说唱\n开口跪\n五月天\n舞台\nLIVE'),('music','mv','音乐','MV','该分区收录为音乐作品配合拍摄或制作的音乐录影带(Music Video)，包含但不限于官方MV或MV预告，以及自制拍摄、剪辑、翻拍的MV。','电子音乐\n电音\n经典歌曲\n韩语MV\n治愈\n说唱\nK-Pop\n欧美MV\n日语MV\n音乐推荐'),('music','original','音乐','原创音乐','以任何题材创作的、以音乐主体为主要原创考量标准的原创歌曲及纯音乐，包括对音乐歌曲中曲的改编、重编曲及remix。','Medly\n纯音乐\n原创音乐\n歌曲\n治愈\n作曲\n听歌\nSANS\nrap\n传说之下'),('music','other','音乐','音乐综合','该分区收录所有无法被收纳到其他音乐二级分区的音乐类视频。包括但不限于个人选集以及任何形式的曲包。','纯音乐\nK-Pop\n音乐\n听歌\n欧美音乐\n音乐综合\n外文歌曲\n成名曲\nMV\n好听'),('music','perform','音乐','演奏','乐器和非传统乐器器材的演奏作品。','SOLO\n贝斯\n电吉他\n指弹\n钢琴曲\n古典音乐\n钢琴演奏\n古筝\n吉他弹唱\n乐器'),('music','rap','音乐','说唱','嘻哈、Rap等风格的音乐作品。','歌曲\n欧美音乐\nMV\n听歌向\n音乐综合\n单曲循环\n纯音乐\nrap\n现场'),('music','tutorial','音乐','音乐教学','该分区收录以音乐教学为目的的内容，包括但不限于声乐教学、乐器教学、编曲制作教学、乐器设备测评等。','教程\n音乐\n声乐教学\n唱歌\n学唱歌\n成人钢琴\n教学\n钢琴教学\n声乐\n歌手'),('music','vocaloid','音乐','VOCALOID·UTAU','以VOCALOID等歌声合成引擎为基础，运用各类音源进行的创作。','AI翻唱\n初音ミク\nACG音乐\nAl\n碧蓝档案\n搞笑\n虚拟UP主\n洛天依\n虚拟歌姬\n周杰伦'),('sports','aerobics','运动','健身','与健身相关的视频，包括但不限于健身、健美、操舞、瑜伽、普拉提、跑步、街健、健康餐、健身小剧场等内容','减脂\n减肥记录\n健康\n坚持\n引体向上\n励志\n打卡\n跳绳\n胸肌\n帅哥'),('sports','athletic','运动','竞技体育','与竞技体育相关的视频，包括但不限于乒乓、羽毛球、排球、赛车等竞技项目的赛事、评述、剪辑、剧情等相关内容','拳击\n羽生结弦\n花样滑冰\n排球\n田径\nwwE\n摔角\n综合格斗\n篮球\n搞笑'),('sports','basketball','运动','篮球','与篮球相关的视频，包括但不限于篮球赛事、教学、评述、剪辑、剧情等相关内容','勇士\n比赛\n单挑\n易建联\n男篮\n杜兰特\n徐静雨\n野球帝\n科比\n中国'),('sports','comprehensive','运动','运动综合','与运动综合相关的视频，包括但不限于钓鱼、骑行、滑板等日常运动分享、教学、Vlog等相关内容','体育\n羽毛球\n篮球'),('sports','culture','运动','运动文化','与运动文化相关的视频，包括但不限于球鞋、球衣、球星卡等运动衍生品的分享、解读，体育产业的分析、科普等相关内容','骑行\n球星卡\n球鞋测评\n球鞋\n传统武术\n训练\n羽毛球\n跑步\n开箱\n极限运动'),('sports','football','运动','足球','与足球相关的视频，包括但不限于足球赛事、教学、评述、剪辑、剧情等相关内容','中超\nC罗\n足球推荐\n体育\n篮球\n国足\n梅西\n英超\n比分预测\n晏城'),('tech','application','科技','软件应用','围绕各类系统软件、应用软件、网站的相关视频','APP\nAIGC\n小程序开发\n免费\n知识\n编程\n游戏\n黑科技\n科技\n电脑'),('tech','computer_tech','科技','计算机技术','软硬件开发/人工智能/大数据/深度学习/IT运维等','系统\nLinux\n编程语言\nIT\n网络安全\n互联网\nJava\n计算机\nC++\n前端'),('tech','digital','科技','数码','手机/电脑/相机/影音智能设备等','笔记本电脑\n科技\n华为\n客制化\n体验\n机械键盘\n相机\n摄影\n键盘\nDIY'),('tech','diy','科技','极客DIY','硬核制作/发明创造/技术创新/极客文化等','极客\n机器人\n3D打印\n维修\n测试\n摄影\n工业\n穿越机\n人工智能\n单片机'),('tech','industry','科技','科工机械','航空航天/工程建设/电子工程/机械制造/海洋工程等','机械\n工业\n新能源\n施工DIY工厂\n智能\n美国\n机器人\n基建'),('virtual','douga','虚拟UP主','动画','以虚拟形象出镜，作品内容和动画相关的视频','虚拟偶像\n虚拟UP主\nAMV\n动画短片\n直播录像\n原创\n音乐\n国漫\n童年\n咒术回战'),('virtual','game','虚拟UP主','游戏','以虚拟形象出镜，作品内容和游戏相关的视频','虚拟偶像\n虚拟UP主\nRPG\n动作游戏\n单机\n原神\n直播录像\n我的世界\n实况解说\n生存游戏\n单机游戏'),('virtual','music','虚拟UP主','音乐','以虚拟形象出镜，作品内容和音乐相关的视频','虚拟UP主\nvup\n虚拟主播\n抒情\n录屏\n流行\n凌音阁\n清唱\n女声\n音乐'),('virtual','other','虚拟UP主','其他','除上述几种类型的其他虚拟UP主相关视频','虚拟偶像\n虚拟UP主\n聊天\n直播录像');
/*!40000 ALTER TABLE `category` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `chat`
--

DROP TABLE IF EXISTS `chat`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `chat` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '唯一主键',
  `user_id` int(11) NOT NULL COMMENT '对象UID',
  `another_id` int(11) NOT NULL COMMENT '用户UID',
  `is_deleted` tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否移除聊天 0否 1是',
  `unread` int(11) NOT NULL DEFAULT '0' COMMENT '消息未读数量',
  `latest_time` datetime NOT NULL COMMENT '最近接收消息的时间或最近打开聊天窗口的时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `from_to` (`user_id`,`another_id`),
  UNIQUE KEY `id` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=0 DEFAULT CHARSET=utf8mb4 COMMENT='聊天表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `chat_detailed`
--

DROP TABLE IF EXISTS `chat_detailed`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `chat_detailed` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '唯一主键',
  `user_id` int(11) NOT NULL COMMENT '消息发送者',
  `another_id` int(11) NOT NULL COMMENT '消息接收者',
  `content` varchar(500) NOT NULL COMMENT '消息内容',
  `user_del` tinyint(4) NOT NULL DEFAULT '0' COMMENT '发送者是否删除',
  `another_del` tinyint(4) NOT NULL DEFAULT '0' COMMENT '接受者是否删除',
  `withdraw` tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否撤回',
  `time` datetime NOT NULL COMMENT '消息发送时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `id` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=0 DEFAULT CHARSET=utf8mb4 COMMENT='聊天记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `comment`
--

DROP TABLE IF EXISTS `comment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `comment` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '评论主id',
  `vid` int(11) NOT NULL COMMENT '评论的视频id',
  `uid` int(11) DEFAULT NULL COMMENT '发送者id',
  `root_id` int(11) NOT NULL DEFAULT '0' COMMENT '根节点评论的id,如果为0表示为根节点',
  `parent_id` int(11) NOT NULL COMMENT '被回复的评论id，只有root_id为0时才允许为0，表示根评论',
  `to_user_id` int(11) NOT NULL COMMENT '回复目标用户id',
  `content` varchar(2000) NOT NULL COMMENT '评论内容',
  `love` int(11) NOT NULL DEFAULT '0' COMMENT '该条评论的点赞数',
  `bad` int(11) DEFAULT '0' COMMENT '不喜欢的数量',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `is_top` tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否置顶 0普通 1置顶',
  `is_deleted` tinyint(4) NOT NULL DEFAULT '0' COMMENT '软删除 0未删除 1已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `id` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=0 DEFAULT CHARSET=utf8mb4 COMMENT='评论表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `danmu`
--

DROP TABLE IF EXISTS `danmu`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `danmu` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '弹幕ID',
  `vid` int(11) NOT NULL COMMENT '视频ID',
  `uid` int(11) NOT NULL COMMENT '用户ID',
  `content` varchar(100) NOT NULL COMMENT '弹幕内容',
  `fontsize` tinyint(4) NOT NULL DEFAULT '25' COMMENT '字体大小',
  `mode` tinyint(4) NOT NULL DEFAULT '1' COMMENT '弹幕模式 1滚动 2顶部 3底部',
  `color` varchar(7) NOT NULL DEFAULT '#FFFFFF' COMMENT '弹幕颜色 6位十六进制标准格式',
  `time_point` double NOT NULL COMMENT '弹幕所在视频的时间点',
  `state` tinyint(4) NOT NULL DEFAULT '1' COMMENT '弹幕状态 1默认过审 2被举报审核中 3删除',
  `create_date` datetime NOT NULL COMMENT '发送弹幕的日期时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `id` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=0 DEFAULT CHARSET=utf8mb4 COMMENT='弹幕表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `favorite`
--

DROP TABLE IF EXISTS `favorite`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `favorite` (
  `fid` int(11) NOT NULL AUTO_INCREMENT COMMENT '收藏夹ID',
  `uid` int(11) NOT NULL COMMENT '所属用户ID',
  `type` tinyint(4) NOT NULL DEFAULT '1' COMMENT '收藏夹类型 1默认收藏夹 2用户创建',
  `visible` tinyint(4) NOT NULL DEFAULT '1' COMMENT '对外开放 0隐藏 1公开',
  `cover` varchar(255) DEFAULT NULL COMMENT '收藏夹封面',
  `title` varchar(20) NOT NULL COMMENT '标题',
  `description` varchar(200) DEFAULT '' COMMENT '简介',
  `count` int(11) NOT NULL DEFAULT '0' COMMENT '收藏夹中视频数量',
  `is_delete` tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否删除 0否 1已删除',
  PRIMARY KEY (`fid`),
  UNIQUE KEY `fid` (`fid`)
) ENGINE=InnoDB AUTO_INCREMENT=0 DEFAULT CHARSET=utf8mb4 COMMENT='收藏夹';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `favorite_video`
--

DROP TABLE IF EXISTS `favorite_video`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `favorite_video` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '唯一标识',
  `vid` int(11) NOT NULL COMMENT '视频ID',
  `fid` int(11) NOT NULL COMMENT '收藏夹ID',
  `time` datetime NOT NULL COMMENT '收藏时间',
  `is_remove` tinyint(4) DEFAULT NULL COMMENT '是否移除 null否 1已移除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `id` (`id`),
  UNIQUE KEY `vid_fid__index` (`vid`,`fid`)
) ENGINE=InnoDB AUTO_INCREMENT=0 DEFAULT CHARSET=utf8mb4 COMMENT='视频收藏夹关系表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `msg_unread`
--

DROP TABLE IF EXISTS `msg_unread`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `msg_unread` (
  `uid` int(11) NOT NULL COMMENT '用户ID',
  `reply` int(11) NOT NULL DEFAULT '0' COMMENT '回复我的',
  `at` int(11) NOT NULL DEFAULT '0' COMMENT '@我的',
  `love` int(11) NOT NULL DEFAULT '0' COMMENT '收到的赞',
  `system` int(11) NOT NULL DEFAULT '0' COMMENT '系统通知',
  `whisper` int(11) NOT NULL DEFAULT '0' COMMENT '我的消息',
  `dynamic` int(11) NOT NULL DEFAULT '0' COMMENT '动态',
  PRIMARY KEY (`uid`),
  UNIQUE KEY `uid` (`uid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息未读数';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user` (
  `uid` int(11) NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(50) NOT NULL COMMENT '用户账号',
  `password` varchar(255) NOT NULL COMMENT '用户密码',
  `nickname` varchar(32) NOT NULL COMMENT '用户昵称',
  `avatar` varchar(500) DEFAULT NULL COMMENT '用户头像url',
  `background` varchar(500) DEFAULT NULL COMMENT '主页背景图url',
  `gender` tinyint(4) NOT NULL DEFAULT '2' COMMENT '性别 0女 1男 2未知',
  `description` varchar(100) DEFAULT NULL COMMENT '个性签名',
  `exp` int(11) NOT NULL DEFAULT '0' COMMENT '经验值',
  `coin` double NOT NULL DEFAULT '0' COMMENT '硬币数',
  `vip` tinyint(4) NOT NULL DEFAULT '0' COMMENT '会员类型 0普通用户 1月度大会员 2季度大会员 3年度大会员',
  `state` tinyint(4) NOT NULL DEFAULT '0' COMMENT '状态 0正常 1封禁 2注销',
  `role` tinyint(4) NOT NULL DEFAULT '0' COMMENT '角色类型 0普通用户 1管理员 2超级管理员',
  `auth` tinyint(4) NOT NULL DEFAULT '0' COMMENT '官方认证 0普通用户 1个人认证 2机构认证',
  `auth_msg` varchar(30) DEFAULT NULL COMMENT '认证说明',
  `create_date` datetime NOT NULL COMMENT '创建时间',
  `delete_date` datetime DEFAULT NULL COMMENT '注销时间',
  PRIMARY KEY (`uid`),
  UNIQUE KEY `uid` (`uid`),
  UNIQUE KEY `username` (`username`),
  UNIQUE KEY `nickname` (`nickname`)
) ENGINE=InnoDB AUTO_INCREMENT=0 DEFAULT CHARSET=utf8 COMMENT='用户表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user_video`
--

DROP TABLE IF EXISTS `user_video`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_video` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '唯一标识',
  `uid` int(11) NOT NULL COMMENT '观看视频的用户UID',
  `vid` int(11) NOT NULL COMMENT '视频ID',
  `play` int(11) NOT NULL DEFAULT '0' COMMENT '播放次数',
  `love` tinyint(4) NOT NULL DEFAULT '0' COMMENT '点赞 0没赞 1已点赞',
  `unlove` tinyint(4) NOT NULL DEFAULT '0' COMMENT '不喜欢 0没点 1已不喜欢',
  `coin` tinyint(4) NOT NULL DEFAULT '0' COMMENT '投币数 0-2 默认0',
  `collect` tinyint(4) NOT NULL DEFAULT '0' COMMENT '收藏 0没收藏 1已收藏',
  `play_time` datetime NOT NULL COMMENT '最近播放时间',
  `love_time` datetime DEFAULT NULL COMMENT '点赞时间',
  `coin_time` datetime DEFAULT NULL COMMENT '投币时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `id` (`id`),
  UNIQUE KEY `uid_vid__index` (`uid`,`vid`)
) ENGINE=InnoDB AUTO_INCREMENT=0 DEFAULT CHARSET=utf8mb4 COMMENT='用户视频关联表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `video`
--

DROP TABLE IF EXISTS `video`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `video` (
  `vid` int(11) NOT NULL AUTO_INCREMENT COMMENT '视频ID',
  `uid` int(11) NOT NULL COMMENT '投稿用户ID',
  `title` varchar(80) NOT NULL COMMENT '标题',
  `type` tinyint(4) NOT NULL DEFAULT '1' COMMENT '类型 1自制 2转载',
  `auth` tinyint(4) NOT NULL DEFAULT '0' COMMENT '作者声明 0不声明 1未经允许禁止转载',
  `duration` double NOT NULL DEFAULT '0' COMMENT '播放总时长 单位秒',
  `mc_id` varchar(20) NOT NULL COMMENT '主分区ID',
  `sc_id` varchar(20) NOT NULL COMMENT '子分区ID',
  `tags` varchar(500) DEFAULT NULL COMMENT '标签 回车分隔',
  `descr` varchar(2000) DEFAULT NULL COMMENT '简介',
  `cover_url` varchar(500) NOT NULL COMMENT '封面url',
  `video_url` varchar(500) NOT NULL COMMENT '视频url',
  `status` tinyint(4) NOT NULL DEFAULT '0' COMMENT '状态 0审核中 1已过审 2未通过 3已删除',
  `upload_date` datetime NOT NULL COMMENT '上传时间',
  `delete_date` datetime DEFAULT NULL COMMENT '删除时间',
  PRIMARY KEY (`vid`),
  UNIQUE KEY `vid` (`vid`)
) ENGINE=InnoDB AUTO_INCREMENT=0 DEFAULT CHARSET=utf8 COMMENT='视频表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `video_stats`
--

DROP TABLE IF EXISTS `video_stats`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `video_stats` (
  `vid` int(11) NOT NULL COMMENT '视频ID',
  `play` int(11) NOT NULL DEFAULT '0' COMMENT '播放量',
  `danmu` int(11) NOT NULL DEFAULT '0' COMMENT '弹幕数',
  `good` int(11) NOT NULL DEFAULT '0' COMMENT '点赞数',
  `bad` int(11) NOT NULL DEFAULT '0' COMMENT '点踩数',
  `coin` int(11) NOT NULL DEFAULT '0' COMMENT '投币数',
  `collect` int(11) NOT NULL DEFAULT '0' COMMENT '收藏数',
  `share` int(11) NOT NULL DEFAULT '0' COMMENT '分享数',
  `comment` int(11) DEFAULT '0' COMMENT '评论数量统计',
  PRIMARY KEY (`vid`),
  UNIQUE KEY `vid` (`vid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='视频数据统计表';
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2024-04-16 17:42:34
