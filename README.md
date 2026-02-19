# Popboo - 爆竹声中一命除

一个跨版本、跨服务端实现的 “爆竹” 效果插件：当竹子及其制品燃烧时，按概率在随机延迟后爆炸，并支持爆炸连锁引爆。

> 项目包含由AI生成的内容。

## 兼容性

- 游戏版本：1.14+（竹子引入版本）；在高版本会自动覆盖更多竹子制品
- 服务端：Bukkit / Spigot / Paper / Purpur / Folia

## 安装

1. 将构建生成的 jar 放入服务器 `plugins/`。
2. 启动服务器，生成默认配置 `plugins/Popboo/config.yml`。
3. 按需修改配置后执行：`/popboo reload`（或重启服务器）。

## 功能说明

### 方块燃烧（打火石/火焰蔓延）

- 当“火”出现/蔓延时，会以火的位置为中心检查六个相邻方块（上下左右前后）。
- 若相邻方块为“竹子及其制品”，则按概率挂载爆竹，并在延迟窗口内爆炸。
- 若该方块最终被火烧毁，且此前已挂载爆竹，则在烧毁瞬间立即引爆（更符合“烧断引爆”的感觉）。

### 熔炉/高炉/烟熏炉燃料燃烧

- 当燃料开始燃烧时，如果燃料是“竹子及其制品”，则以炉子方块位置按概率挂载爆竹，并在延迟窗口内爆炸。
- 带炉子冷却（`furnace.cooldownTicks`），避免同一炉子过于密集地触发。

### 连锁引爆

- 任意爆炸发生时，会扫描附近已挂载的爆竹点并引爆（受 `chainRadius` 与 `chainMaxCount` 限制）。

## “竹子及其制品”判定规则

为保证跨版本兼容，本插件默认采用 Material 名称规则：

- `Material.name()` 包含 `BAMBOO` 的方块/物品：都视为竹子相关（例如 `BAMBOO_PLANKS`、`BAMBOO_TRAPDOOR` 等）
- 额外白名单：`SCAFFOLDING`

如果你发现某个版本里存在“竹子制品但名称不含 BAMBOO”，打开 `debug: true` 后把控制台输出的 `fuel=xxx` 发我，我会把它加到白名单里。

## 命令与权限

- `/popboo reload`：重载配置
- 权限：`popboo.admin`（默认 OP 拥有）

## 排障（强烈建议）

当出现“某个竹子制品不触发”时：

1. 将 `debug: true`
2. 执行 `/popboo reload`
3. 复现一次（点火或燃料）
4. 查看控制台关键日志：
   - `furnace_event ... fuel=XXX`
   - `furnace_armed ...` 或 `furnace_arm_skip ...`
   - `block_armed ...` 或 `block_arm_skip ...`
   - `chain_triggered ...`

## 配置

默认配置文件带中英文注释：

```yaml
# Popboo 配置文件（UTF-8）
# Popboo config (UTF-8)
#
# 概率类参数范围建议为 0.0-1.0；ticks 为游戏刻（20 ticks = 1 秒）
# Probability values are recommended in 0.0-1.0; ticks are game ticks (20 ticks = 1 second)

# 是否启用插件
# Enable/disable plugin
enabled: true

# 挂载爆竹概率
# Chance to arm a "popboo" (delayed explosion)
chance:
  # 方块点火/火焰蔓延时，竹子方块/竹子制品被“挂载”的概率
  # Chance to arm when bamboo-related blocks are ignited / fire spreads nearby
  blockIgnite: 0.6
  # 竹子相关物品作为燃料开始燃烧时，被“挂载”的概率
  # Chance to arm when bamboo-related items start burning as furnace fuel
  furnaceFuel: 0.3

# 烧毁兜底（可选）
# Burn fallback (optional)
#
# 说明：默认情况下，只有“已挂载”的方块在烧毁时才会立即引爆。
# 当你打开兜底后：即使没来得及挂载，在方块烧毁瞬间也会按概率引爆（会更吵/更频繁）。
# Note: by default only "armed" blocks explode when they burn away.
# When enabled, even unarmed bamboo-related blocks may explode on burn (noisier/more frequent).
blockBurnFallback:
  # 是否启用烧毁兜底
  # Enable burn fallback
  enabled: false
  # 烧毁兜底触发概率
  # Burn fallback chance
  chance: 0.2

# 延迟爆炸时间窗口（ticks）
# Delayed explosion window (ticks)
delayTicks:
  # 最小延迟
  # Min delay
  min: 10
  # 最大延迟
  # Max delay
  max: 60

# 爆炸参数
# Explosion settings
explosion:
  # 爆炸威力（越大伤害/破坏越强）
  # Explosion power (higher = more damage/breaking)
  power: 4.5
  # 是否产生火焰
  # Whether explosions create fire
  createFire: false
  # 是否破坏方块
  # Whether explosions break blocks
  breakBlocks: true
  # 是否开启连锁引爆（附近已挂载点会被引爆）
  # Enable chain detonation (nearby armed points are triggered)
  chainEnabled: true
  # 连锁半径（方块距离）
  # Chain radius (in blocks)
  chainRadius: 6
  # 单次爆炸最多连锁引爆数量（安全阀）
  # Max chained triggers per explosion (safety cap)
  chainMaxCount: 20

# 炉子相关
# Furnace settings
furnace:
  # 同一个炉子位置的触发冷却（ticks），避免燃料连续触发过密爆炸
  # Cooldown per furnace location (ticks) to avoid spam
  cooldownTicks: 40

# 调试日志开关（建议排查问题时打开）
# Debug logs switch (enable when troubleshooting)
debug: false

```

常用调参建议：

- 想更频繁：提高 `chance.blockIgnite` / `chance.furnaceFuel`，并缩短 `delayTicks` 范围
- 想更凶：提高 `explosion.power`，并保持 `breakBlocks: true`
- 想更“爆竹”但不炸地形：设置 `breakBlocks: false`

## 鸣谢名单
- pama1234