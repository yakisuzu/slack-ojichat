# ModelingGuidelines
## classの種類
### ValueObject
- 単位  
  - すべてimmutable  
  - enum的な  
  - 同一性はない  
- 実装  
  - case class  
- 依存  
  - なし  

### Entity
- 単位  
  - 一意なキーが存在する  
  - キーはimmutable  
  - キー以外がmutable(になり得る)  
  キー以外の変更があるときは、新しいインスタンスを返す  
- 実装  
  - case class  
- 依存  
  - Entity/ValueObject  

### Aggregate
- 単位  
  - 整合性のあるまとまり  
  - 同一トランザクションで処理する必要のある単位  
  - Entityの集まり  
- 実装  
  - case class  
  - 全てrootのEntityのAPIを経由する  
- 依存  
  - Entity/ValueObject  

### Service
- 単位  
  - 動詞によってひとつ  
  - ちょっと違う動作はextends [TODO]  
- 実装  
  - trait  
- 依存  
  - Entity/ValueObject  
  - Repository  

### Module
- 近い概念はpackageを同じにする  

### Repository
- 単位  
  - リポジトリに対するオペレーションの種類ごと  
- 実装  
  - trait  
- 依存  
  - Dao  
  - Entity/ValueObject  

### Dao
- 単位  
  - 接続先によってひとつ  
- 実装  
  - trait  
- 依存  
  - Entity/ValueObject  

### Factory
- 単位  
  - なにかとつくる  
- 実装  
  - object  
- 依存  
  - Entity/ValueObject  
  - Service  
  - Repository  
  - Dao  
  - Aggregate  


## レイヤーの整理
オニオンアーキテクチャをベースに各クラスをざっと配置  
厳密な定義ではなく、レイヤーのイメージとして使う  

### UI(Presentation) or Infrastructure
- Dao  
RESTやDB(ORM)を想定  
実際のエンドポイントやSQLを隠蔽する  
ドメインロジックを含ませなてはいけない  
(SQLがドメインロジックという問題はFIXME)  

### ApplicationService
- MVCでいうController的なもの  
FactoryでServiceを初期化し、利用していくだけ  

### DomainService
- Service(+Factory)  
Aggregateでまとめられない複数のEntityを操作するときの場所  
Repositoryに関連のある処理もやる  
単一のEntity/ValueObject/Aggregateで収まる処理はServiceではない  

- Repository(+Factory)  
ドメイン的な値から、Daoの引数へ整形する  
Daoの戻り値から、ドメイン的な扱いたい単位へ変換する  
複数selectが必要な場合など、Repositoryで隠蔽する  

### DomainModel
- Entity/ValueObject(+Factory)  
振る舞いを持つデータを、同じ場所でまとめる  

- Aggregate(+Factory)  
一貫性が必要な単位をまとめる  
無理に作らなくていい  

### Testの対象
- Entity/ValueObject  
- Aggregate  
- Service  
- Repository  

