<template>
  <div class="app-container">
    <el-alert title="数据统计已整合到首页仪表盘" type="info" :closable="false" show-icon style="margin-bottom:20px">
      <span slot="title">数据统计面板已移至首页，点击下方按钮可跳转查看。</span>
    </el-alert>
    <el-button type="primary" icon="el-icon-s-home" @click="$router.push('/')">前往首页仪表盘</el-button>
  </div>
</template>

<script>
export default {
  name: "OverdueStatistics"
}
</script>
