import requests
import os
from time import sleep
import re
from concurrent.futures import ThreadPoolExecutor, as_completed
from threading import Lock

# ========== 配置区 ==========
API_KEY = "eb0db7af5dc4377642db022fc1c5fb88"
SAVE_ROOT = "./tmdb_all_50_movie_img"
IMG_SIZE = "original"
BASE_IMG_URL = f"https://image.tmdb.org/t/p/{IMG_SIZE}/"
BASE_API = "https://api.themoviedb.org/3"

PROXY_URL = "http://127.0.0.1:7897"
proxies = {"http": PROXY_URL, "https": PROXY_URL}

MAX_WORKERS = 12          # 并发线程数（可根据网络调整 8-16）
# ===========================

print_lock = Lock()

def mkdir_path(path):
    if not os.path.exists(path):
        os.makedirs(path)

def safe_filename(name: str) -> str:
    name = re.sub(r'[\\/:*?"<>|]', '_', name)
    return name.strip()

def download_img(url, save_path):
    if os.path.exists(save_path) and os.path.getsize(save_path) > 10000:  # 已存在且大小合理则跳过
        with print_lock:
            print(f"⏭️ 已存在 → {save_path}")
        return True
    
    try:
        resp = requests.get(url, timeout=15, proxies=proxies)
        if resp.status_code == 200:
            with open(save_path, "wb") as f:
                f.write(resp.content)
            with print_lock:
                print(f"✅ 下载成功 → {save_path}")
            return True
        else:
            with print_lock:
                print(f"⚠️ 资源不存在：{url}")
            return False
    except Exception as e:
        with print_lock:
            print(f"❌ 下载失败 {url} | {str(e)}")
        return False

def get_movie_all_info(movie_tmdb_id):
    try:
        # 1. 电影详情
        detail_url = f"{BASE_API}/movie/{movie_tmdb_id}?api_key={API_KEY}&language=zh-CN"
        detail_res = requests.get(detail_url, proxies=proxies, timeout=15).json()

        movie_name = detail_res.get("title") or detail_res.get("original_title", f"未知电影_{movie_tmdb_id}")
        movie_name = safe_filename(movie_name)
        
        poster_path = detail_res.get("poster_path")

        # 创建文件夹
        movie_folder = os.path.join(SAVE_ROOT, movie_name)
        poster_folder = os.path.join(movie_folder, "poster")
        director_folder = os.path.join(movie_folder, "director")
        actor_folder = os.path.join(movie_folder, "actor")

        mkdir_path(poster_folder)
        mkdir_path(director_folder)
        mkdir_path(actor_folder)

        # 下载海报
        if poster_path:
            poster_url = BASE_IMG_URL + poster_path
            save_file = os.path.join(poster_folder, f"{movie_name}.jpg")
            download_img(poster_url, save_file)

        # 2. 演职员信息
        credit_url = f"{BASE_API}/movie/{movie_tmdb_id}/credits?api_key={API_KEY}&language=zh-CN"
        credit_res = requests.get(credit_url, proxies=proxies, timeout=15).json()

        # 导演
        directors = [c for c in credit_res.get("crew", []) if c.get("job") == "Director"]
        top_cast = credit_res.get("cast", [])[:4]

        # 下载导演
        for d in directors:
            name = safe_filename(d["name"])
            if d.get("profile_path"):
                url = BASE_IMG_URL + d["profile_path"]
                save_path = os.path.join(director_folder, f"{name}.jpg")
                download_img(url, save_path)

        # 下载演员
        for a in top_cast:
            actor_name = safe_filename(a["name"])
            if a.get("profile_path"):
                url = BASE_IMG_URL + a["profile_path"]
                save_path = os.path.join(actor_folder, f"{actor_name}.jpg")
                download_img(url, save_path)

        with print_lock:
            print(f"🎉 完成：{movie_name} (ID: {movie_tmdb_id})")

    except Exception as e:
        with print_lock:
            print(f"❌ 处理电影 {movie_tmdb_id} 失败: {str(e)}")

# ====================== 主程序 ======================
if __name__ == "__main__":
    movie_id_list = [
        9470,      # 功夫
        578206,      # 唐伯虎点秋香
        85502,      # 东成西就
        9056,      # 警察故事
        21519,      # A 计划
        45380,      # 疯狂的石头
        362682,      # 夏洛特烦恼
        479306,      # 羞羞的铁拳
        51306,      # 人在囧途
        151743,      # 人再囧途之泰囧
        10775,      # 无间道
        11647,      # 无间道 2
        1117362,      # 暗战
        11782,      # 神探
        137409,      # 寒战
        32582,      # 风声
        252672,      # 无双
        344556,      # 烈日灼心
        11423,      # 杀人回忆
        101,      # 这个杀手不太冷
        21835,      # 大话西游之大圣娶亲
        262,      # 喜剧之王
        37185,      # 甜蜜蜜
        105680,      # 志明与春娇
        105680,      # 春娇与志明
        79824,      # 失恋 33 天
        494900,      # 前任 3：再见前任
        639,      # 当哈利遇到莎莉
        76,      # 爱在黎明破晓前
        80,      # 爱在日落黄昏时
        535167,      # 流浪地球
        842675,      # 流浪地球 2
        335984,      # 银翼杀手 2049
        63,      # 十二猴子
        220289,      # 彗星来的那一夜
        4977,      # 红辣椒
        14836,      # 鬼妈妈
        372058,      # 你的名字。
        615453,      # 哪吒之魔童降世
        271706,      # 大鱼海棠
        152601,      # 她
        122906,      # 时空恋旅人
        11104,      # 重庆森林
        843,      # 花样年华
        51730,      # 鹿鼎记
        51731,      # 鹿鼎记 2 神龙教
        47648,      # 武状元苏乞儿
        32517,      # 审死官
        41387,      # 国产凌凌漆
        37702,      # 大内密探零零发
        52324,      # 百变星君
        41343,      # 食神
        132183,      # 行运一条龙
        53281,      # 千王之王 2000
        73414,      # 新精武门 1991
        73417,      # 漫画威龙
        64083,      # 97 家有喜事
        60145,      # 家有喜事 1992
        53163,      # 破坏之王
        45487,      # 算死草
        740010,      # 盖世豪侠
        56123,      # 豪门夜宴
        138960,      # 师兄撞鬼
        135061,      # 无敌幸运星
        50507,      # 非洲和尚
        21521,      # A 计划续集
        10753,      # 警察故事续集
        11134,      # 警察故事 3 超级警察
        9404,      # 警察故事 4 简单任务
        11205,      # 快餐车
        21733,      # 飞龙猛将
        10975,      # 飞鹰计划
        33542,      # 红番区
        436047,      # 我是谁
        2109,      # 尖峰时刻 1
        5175,      # 尖峰时刻 2
        5174,      # 尖峰时刻 3
        18764,      # 双龙会
        1297842,      # 奇迹
        25676,      # 宝贝计划
        379149,      # 英伦对决
        98567,      # 十二生肖
        20083,      # 新宿事件
        10348,      # 霹雳火
        1061990,      # 城市猎人
        51730,      # 鹿鼎记之皇城争霸
        118376,      # 新难兄难弟
        36266,      # 三轮车夫
        45438,      # 东京攻略
        10265,      # 韩城攻略
        45379,      # 天下无双
        187749,      # 侠客行
        41158,      # 暗花
        45470,      # 流氓医生
        45473,      # 有情饮水饱
        146116,      # 幸运超人
        44865,      # 一代宗师
        14310,      # 无间道 3 终极无间
        70697,      # 新仙鹤神针
        278070,      # 等着你回来
        78027,      # 同居蜜友
        247999,      # 黑狱断肠歌
        81128,      # 天若有情
        217631,      # 天若有情 2 天长地久
        9443,      # 烈火战车
        102263,      # 黑马王子
        45418,      # 孤男寡女
        43152,      # 瘦身男女
        33840,      # 龙凤斗
        645788,      # 门徒
        32233,      # 大只佬
        9581,      # 全职杀手
        14392,      # 投名状
        14808,      # 墨攻
        416249,      # 寒战 2
        91186,      # 桃姐
        1314786,      # 风暴
        212996,      # 五亿探长雷洛传 1
        358306,      # 雷洛传 2 父子情仇
        16269,      # 赌神 1
        443294,      # 枪火
        13807,      # 放逐
        25536,      # PTU
        25664,      # 柔道龙虎榜
        58570,      # 再见阿郎
        67793,      # 单身男女
        287420,      # 单身男女 2
        90147,      # 夺命金
        194523,      # 盲探
        58776,      # 真心英雄
        45303,      # 暗战 2
        86409,      # 高海拔之恋 2
        543504,      # 神探大战
        42120,      # 龙虎风云
        78031,      # 七年很痒
        32151,      # 铁三角
        24163,      # 旺角卡门
        18311,      # 阿飞正传
        40751,      # 东邪西毒
        18329,      # 春光乍泄
        1989,      # 蓝莓之夜
        846716,      # 爱神之手
        11220,      # 堕落天使
        345674,      # 摆渡人
        284298,      # 心花路放
        33106,      # 疯狂的赛车
        532753,      # 我不是药神
        361613,      # 港囧
        603770,      # 囧妈
        574037,      # 疯狂外星人
        535170,      # 一出好戏
        638032,      # 夺冠
        902478,      # 爱情神话
        803266,      # 奇迹笨小孩
        945703,      # 保你平安
        538331,      # 西虹市首富
        551127,      # 李茶的姑妈
        743413,      # 温暖的抱抱
        801803,      # 独行月球
        798753,      # 超能一家人
        923192,      # 这个杀手不太冷静
        1462229,      # 飞驰人生
        1228891,      # 飞驰人生 2
        1077295,      # 人生路不熟
        1112014,      # 好像也没那么热血沸腾
        527415,      # 龙虾刑警
        796120,      # 发财日记
        19503,      # 赌神 2
        10835,      # 喋血双雄
        11471,      # 英雄本色
        18305,      # 英雄本色 2
        2137,      # 风云雄霸天下
        334557,      # 踏血寻梅
        492851,      # 密战
        76257,      # 阿郎的故事
        47423,      # 纵横四海
        447954,      # 春娇救志明
        210498,      # 飞虎出征
        52776,      # 72 家租客
        57880,      # 我爱 HK 开心万岁
        37850,      # 同门
        9060,      # 饺子
        11128,      # 烈火雄心
        41471,      # 战狼 1
        452557,      # 战狼 2
        13127,      # 杀破狼 1
        1480387,      # 暗流
        60672,      # 暗流 2：末日天使
        2110,      # 绿芥刑警
        10799,      # 决战帝国
        44214,      # 黑天鹅
        752,      # V 字仇杀队
        287947,      # 雷神 1
        76338,      # 雷神 2：黑暗世界
        376866,      # 第一夫人
        290859,      # 终结者：黑暗命运
        36955,      # 真实的谎言
        106,      # 铁血战士 1
        9268,      # 蒸发密令
        493550,      # 魔鬼司令
        107846,      # 金蝉脱壳
        87101,      # 终结者：创世纪
        245891,      # 疾速追杀 1
        324552,      # 疾速追杀 2
        458156,      # 疾速追杀 3
        561,      # 康斯坦丁
        76544,      # 太极侠
        604,      # 黑客帝国 2 重装上阵
        605,      # 黑客帝国 3 矩阵革命
        1648,      # 比尔和泰德历险记
        550,      # 搏击俱乐部
        4922,      # 本杰明?巴顿奇事
        807,      # 七宗罪
        16869,      # 无耻混蛋
        787,      # 史密斯夫妇
        652,      # 特洛伊
        60308,      # 点球成金
        78,      # 银翼杀手 1982
        85,      # 夺宝奇兵 1 法柜奇兵
        87,      # 夺宝奇兵 2 魔域奇兵
        64690,      # 亡命驾驶
        369972,      # 登月第一人
        46705,      # 蓝色情人节
        8960,      # 全民超人汉考克
        1111737,      # 极寒之城
        335983,      # 毒液：致命守护者
        49026,      # 蝙蝠侠：黑暗骑士崛起
        210479,      # 洛克
        132344,      # 爱在午夜降临前
        59436,      # 午夜巴黎
        18947,      # 海盗电台
        1138887,      # 少年时代
        19345,      # 黑寡妇
        76600,      # 阿凡达 2 水之道
        124905,      # 哥斯拉 (2014)
        10999,      # 独闯龙潭
        844,      # 2046
        76341,      # 疯狂的麦克斯 4：狂暴之路
        68726,      # 环太平洋
        603,      # 黑客帝国
        280,      # 终结者 2：审判日
        83533,      # 阿凡达
        4638,      # 热血警探
        747,      # 僵尸肖恩
        107985,      # 世界尽头
        107,      # 偷拐抢骗
        207703,      # 王牌特工：特工学院
        343668,      # 王牌特工 2：黄金圈
        508,      # 真爱至上
        1272149,      # BJ 单身日记
        170,      # 惊变 28 天
        1562,      # 惊变 28 周
        272,      # 蝙蝠侠：侠影之谜
        157336,      # 星际穿越
        374720,      # 敦刻尔克
        530915,      # 1917
        128,      # 幽灵公主
        10515,      # 天空之城
        8392,      # 龙猫
        16859,      # 魔女宅急便
        33320,      # 千年女优
        13398,      # 东京教父
        10494,      # 未麻的部屋
        1359916,      # 秒速 5 厘米
        198375,      # 言叶之庭
        568160,      # 天气之子
        916224,      # 铃芽之旅
        311054,      # 花与爱丽丝杀人事件
        315837,      # 攻壳机动队
        149,      # 阿基拉
        18148,      # 东京物语
        50759,      # 秋刀鱼之味
        496243,      # 寄生虫
        110415,      # 雪国列车
        381283,      # 母亲
        1255,      # 汉江怪物
        4550,      # 亲切的金子
        290098,      # 小姐
        13855,      # 追击者
        57361,      # 黄海
        1412113,      # 鱿鱼游戏
        491584,      # 燃烧
        255709,      # 素媛
        637920,      # 七号房的礼物
        242452,      # 辩护人
        81481,      # 熔炉
        108433,      # 新世界
        397567,      # 与神同行：罪与罚
        518068,      # 与神同行 2：因与缘
        479718,      # 犯罪都市
        619803,      # 犯罪都市 2
        705996,      # 分手的决心
    ]

    print(f"🚀 启动并发爬取 {len(movie_id_list)} 部电影 (并发线程: {MAX_WORKERS})...\n")

    with ThreadPoolExecutor(max_workers=MAX_WORKERS) as executor:
        future_to_id = {executor.submit(get_movie_all_info, mid): mid for mid in movie_id_list}
        
        for future in as_completed(future_to_id):
            mid = future_to_id[future]
            try:
                future.result()
            except Exception as e:
                print(f"❌ 任务异常 ID {mid}: {e}")

    print("\n🎊 全部任务执行完毕！")