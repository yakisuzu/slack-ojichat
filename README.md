# slack-ojichat

## なんだこれは
オジサンとslackで会話できるヨ。  

## デプロイ
### 1. オジサンの名前を決める
  - `HEROKU_APP_NAME` としていろいろ使っていく  
  
### 2. このリポジトリをForkする
  - たぶん  

### 3. herokuの設定
  - アカウント作成  
  実行環境としてherokuを使っているので、アカウント作成する  
  - API_KEY作成  
  CircleCIから `HEROKU_API_KEY` として利用する  
  https://help.heroku.com/PBGP6IDE/how-should-i-generate-an-api-key-that-allows-me-to-use-the-heroku-platform-api  
  `heroku authorizations:create`  
  - create new app  
  とりあえずappを作る  
  - app nameの設定  
  `Setting > Name` にてオジサンの名前をつける（ `HEROKU_APP_NAME` ）  
  - 実行時の環境変数  
    - `SLACK_TOKEN` の登録  
    `Setting > Config Vars` に `SLACK_TOKEN` を登録する  
    slackのAPPから `Bots` を登録し、 `API Token` を取得する  

### 4. CircleCIの設定
  - ビルド時の環境変数  
    - PROJECT SETTINGSのEnvironment Variables  
    - `HEROKU_APP_NAME` の登録  
    - `HEROKU_API_KEY` の登録  
  - ビルドの有効化  
  masterを元にオジサンがデプロイされる  
  `heroku logs -a ${HEROKU_APP_NAME}` でログ確認  
