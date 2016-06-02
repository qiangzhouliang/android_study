强哥音乐项目总结：
一、技术点：
（1）核心组件：
    Activity、Service、CP调运、Fragment、ViewPager、Handler、AsyncTask、SharedPreference
（2）第三方组件：
    PagerSlidingTabStrip、Quichroll、OKHttpClient、xUtils、Jsoup、AndroidLrcView
（3）设计模式：
    模板方法模式  （BaseActivity跟Activity之间的调运）
    策略模式     （把可变的地方进行一个抽象）
    观察着模式    （接口的回调）

二、功能实现：
    1、我的音乐界面
    2、音乐播放界面
    3、歌词同步界面
    4、我喜欢界面
    5、最近播放界面
    6、网络推荐界面
    7、下载界面

三、类文件说明：

    CodingkePlayerApp: 自定义应用的application类
    BaseActivity：Activity基类
    SplashActivity：闪屏页
    MainActivity：播放器主界面
    PlayActivity：播放界面
    PlayRecordListActivity：最近播放记录列表界面
    MyLikeMusicListActivity: 我喜欢的而音乐界面
    NetMusicListFragment：网络推荐列表界面
    MyMusicListFragment：我的音乐列表界面
    DownloadDialogFragment：下载界面
    PlayService：播放器核心服务类

    Util：工具
    AppUtils：应用程序全局工具类
    Constant：全局常量类
    DownloadUtils：下载功能工具类
    MediaUtils：本地音乐查找工具类
    SearchMusicUtils：网络音乐搜索工具类

    Adapter：适配器
    MusicListAdater：音乐列表适配器
    NetMusicListAdater：网络音乐列表适配器

    VO: Bean类
    Mp3Info：音乐类
    SearchResult：音乐搜索类
