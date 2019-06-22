# slack-ojichat

## なんだこれは
オジサンとslackで会話できるヨ。  

## デプロイ
### 1. オジサンの名前を決める
- `HEROKU_APP_NAME` としていろいろ使っていく  
 
### 2. このリポジトリをForkする
- たぶん  

### 3. slackの設定
- APPでbot作成  
slackのAPPから `Bots` を登録し、 `API Token` を取得する（ `SLACK_TOKEN` ）  
![image](https://user-images.githubusercontent.com/2022475/59964888-ef32af80-9541-11e9-80e8-7caf60b668e8.png)  
- アイコンを設定する  
これは本質的な設定であり、絶対にスキップしないこと、オジサンにリアルな人間性を与えます  
![image](https://user-images.githubusercontent.com/2022475/59964935-90216a80-9542-11e9-81b3-30dc48d3c2b7.png)  

### 4. herokuの設定
- アカウント作成  
実行環境としてherokuを使っているので、アカウント作成する  
- API_KEY作成（ `HEROKU_API_KEY` ）  
https://help.heroku.com/PBGP6IDE/how-should-i-generate-an-api-key-that-allows-me-to-use-the-heroku-platform-api  
`heroku authorizations:create`  
- create new app  
とりあえずappを作る  
- app nameの設定  
`Setting > Name` にてオジサンの名前をつける（ `HEROKU_APP_NAME` ）  
- 実行時の環境変数  
  - `Setting > Config Vars` に登録する  
  - `SLACK_TOKEN` の登録  
  - `HEROKU_APP_NAME` の登録  
- 設定後  
![image](https://user-images.githubusercontent.com/2022475/59964969-3e2d1480-9543-11e9-8ab2-5b2602f492c6.png)  

### 5. CircleCIの設定
- ビルド時の環境変数  
  - PROJECT SETTINGSのEnvironment Variables  
  - `HEROKU_APP_NAME` の登録  
  - `HEROKU_API_KEY` の登録  
- 設定後  
![image](https://user-images.githubusercontent.com/2022475/59964988-77658480-9543-11e9-869a-e2b60d582c93.png)  
- ビルドの有効化  
masterを元にオジサンがデプロイされる  

### 6. 動作確認
- ログ  
`heroku logs -a ${HEROKU_APP_NAME}` でログ確認  
- DynoがONになっていること  
OFFのときは動いていない  
`Resources > Free Dynos` から変更できる  
![image](https://user-images.githubusercontent.com/2022475/59965018-c90e0f00-9543-11e9-8763-4306cd58169c.png)  
