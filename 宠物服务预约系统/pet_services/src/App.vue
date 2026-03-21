<template>
  <div id="app">
    <router-view />
  </div>
</template>

<style lang="less">
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html,
body,
#app {
  height: 100%;
}

body {
  font-family: "PingFang SC", "Microsoft YaHei", -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif;
  background: linear-gradient(180deg, #f6f9ff 0%, #f2f6fc 100%);
  color: #303133;
}

ul, li {
  list-style: none;
}

::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}

::-webkit-scrollbar-thumb {
  background: rgba(102, 166, 255, 0.35);
  border-radius: 999px;
}

::-webkit-scrollbar-track {
  background: transparent;
}

.el-card {
  border-radius: 14px !important;
  border: 1px solid #ecf2ff !important;
  box-shadow: 0 10px 24px rgba(80, 120, 200, 0.08) !important;
}

.el-button--primary {
  background: linear-gradient(135deg, #5b8cff, #6ea8ff) !important;
  border-color: transparent !important;
}

.el-table {
  border-radius: 10px;
  overflow: hidden;
}
</style>
