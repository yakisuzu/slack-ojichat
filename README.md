# slack-ojichat

## なんだこれは
オジサンとslackで会話できるヨ。  

## オジサンの行動
### 1. オジサンへメンションすると返信がくる
![image](https://user-images.githubusercontent.com/2022475/59965162-5867f200-9545-11e9-8960-9dff56d7f6ad.png)  
いい感じに話しかけるといい感じにチャンネルが盛り上がる  
- 朝きたとき  
- 帰るとき  
- 仕事に飽きたとき  

### 2. オジサンに話しかけてないのに突然リアクションで絡んでくる
オジサンが参加しているチャンネル内の発言に対し、  
きまぐれ(50%)に、登録してある絵文字からランダムに選んで反応してくる  
![image](https://user-images.githubusercontent.com/2022475/59970526-8a0fa600-95a3-11e9-9aa5-e61ec5c9d46e.png)  
たまに会話が成立する  

![image](https://user-images.githubusercontent.com/2022475/59970549-01ddd080-95a4-11e9-971b-a1c4a220720e.png)  
たまに煽られる  

### 3. スレッド使うとめっちゃうざい
オジサンが参加しているチャンネル内で、スレッドを使ってしまうと、
めちゃくちゃ話しかけてきて、会話が台無しになる

### ex. 夜は早めに寝てるし、朝はわりと早い
`06:00 ~ 21:00` がオジサンの活動時間  
(herokuの無料枠に納めています)  

## デプロイ
### 1. オジサンの名前を決める
- `HEROKU_APP_NAME` としていろいろ使います
 
### 2. このリポジトリをForkする
- たぶん  

### 3. slackの設定
- APPでbot作成  
slackのAPPから `Bots` を登録し、 `API Token` を取得する  
`SLACK_TOKEN` として使う  
![image](https://user-images.githubusercontent.com/2022475/59964888-ef32af80-9541-11e9-80e8-7caf60b668e8.png)  
- アイコンを設定する  
これは本質的な設定であり、絶対にスキップしないこと、オジサンにリアルな人間性を与えます  
![image](https://user-images.githubusercontent.com/2022475/59964935-90216a80-9542-11e9-81b3-30dc48d3c2b7.png)  

### 4. herokuの設定
- アカウント作成  
実行環境としてherokuを使っているので、アカウント作成する  
- API_KEY作成  
`HEROKU_API_KEY` として使う  
https://help.heroku.com/PBGP6IDE/how-should-i-generate-an-api-key-that-allows-me-to-use-the-heroku-platform-api  
`heroku authorizations:create`  
- create new app  
とりあえずappを作る  
- app nameの設定  
`Setting > Name` にてオジサンの名前をつける  
`HEROKU_APP_NAME` として使う  
- 設定後  
![image](https://user-images.githubusercontent.com/2022475/108371284-f17e6d00-7240-11eb-98ea-8045e1b990c4.png)  

### 5. CircleCIの設定
- ビルド時の環境変数  
  - PROJECT SETTINGSのEnvironment Variables  
  - `HEROKU_API_KEY` の登録  
  - `HEROKU_APP_NAME` の登録
  - `SLACK_TOKEN` の登録
- 設定後  
![image](https://user-images.githubusercontent.com/2022475/108371497-27bbec80-7241-11eb-82fc-b41d15d5fd3d.png)  
- ビルドの有効化  
masterを元にオジサンがデプロイされる  

### 6. 動作確認
- ログ  
`heroku logs -a ${HEROKU_APP_NAME}` でログ確認  
- DynoがONになっていること  
OFFのときは動いていない  
`Resources > Free Dynos` から変更できる  
![image](https://user-images.githubusercontent.com/2022475/59965018-c90e0f00-9543-11e9-8763-4306cd58169c.png)  
