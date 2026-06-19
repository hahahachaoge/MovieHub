import os
import shutil
import yt_dlp

def clean_and_download_trailers():
    # 1. 你的电影文件夹基础路径
    base_dir = r"C:\Users\21065\Desktop\hahaha\JavaWeb大作业\tmdb_all_movie_img"
    
    # 2. 你的链接文件路径
    txt_file = "trailer_links.txt"
    
    # 3. 【全新更新】指向你新解压的 ffmpeg.exe 绝对路径
    ffmpeg_path = r"C:\Users\21065\Desktop\hahaha\JavaWeb大作业\ffmpeg-master-latest-win64-gpl-shared\bin\ffmpeg.exe" 

    if not os.path.exists(txt_file):
        print(f"找不到文件: {txt_file}")
        return

    with open(txt_file, 'r', encoding='utf-8') as f:
        lines = f.readlines()

    for line in lines:
        line = line.strip()
        
        # 自动过滤掉类似 这样的干扰字符
        if '[source' in line:
            if ']' in line:
                line = line.split(']')[-1].strip()
        
        # 如果这一行没有 # 号（即没有电影名），或者为空，则跳过
        if not line or '#' not in line:
            continue
            
        # 强大的文本切割：完美应对多个 # 号或空格
        parts = line.split('#')
        url = parts[0].strip()
        movie_name = parts[1].strip()

        # 过滤掉一些 URL 为空、电影名为空，或者带有“未找到”标记的异常行
        if not url or not movie_name or "未找到" in movie_name:
            continue

        # 构建目标文件夹路径
        movie_dir = os.path.join(base_dir, movie_name)
        trailer_dir = os.path.join(movie_dir, "trailer")

        # 清理旧的未合并的残余文件夹
        if os.path.exists(trailer_dir):
            print(f"[{movie_name}] 正在清理旧的未合并文件...")
            shutil.rmtree(trailer_dir)
        
        # 重新创建干净的 trailer 文件夹
        os.makedirs(trailer_dir, exist_ok=True)

        # 配置 yt-dlp 参数
        ydl_opts = {
            # 强制最佳 MP4 视频 + M4A 音频合并
            'format': 'bestvideo[ext=mp4]+bestaudio[ext=m4a]/best[ext=mp4]/best',
            # 指定输出路径和文件名
            'outtmpl': os.path.join(trailer_dir, f'{movie_name}.%(ext)s'),
            # 【核心突破】强制指定新路径下解除锁定后的 ffmpeg 绝对路径
            'ffmpeg_location': ffmpeg_path,
            'ignoreerrors': True,
            'quiet': False, 
            'no_warnings': True,
        }

        print(f"[{movie_name}] 开始全新下载并合并...")
        
        try:
            with yt_dlp.YoutubeDL(ydl_opts) as ydl:
                ydl.download([url])
            print(f"[{movie_name}] 成功！音视频已完美自动合并！\n" + "-"*30)
        except Exception as e:
            print(f"[{movie_name}] 出现异常: {e}\n" + "-"*30)

if __name__ == "__main__":
    clean_and_download_trailers()