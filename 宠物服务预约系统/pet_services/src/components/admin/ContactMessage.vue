<template>
  <div class="admin-contact-message">
    <div class="toolbar-card">
      <el-breadcrumb separator="/" separator-class="el-icon-arrow-right">
        <el-breadcrumb-item :to="'/adminHome'">首页</el-breadcrumb-item>
        <el-breadcrumb-item>{{ this.$route.meta.title }}</el-breadcrumb-item>
      </el-breadcrumb>

      <div class="toolbar-actions">
        <el-select v-model="status" clearable placeholder="处理状态" style="width: 180px;">
          <el-option label="全部" :value="null" />
          <el-option label="未处理" :value="0" />
          <el-option label="已处理" :value="1" />
        </el-select>
        <el-button type="primary" icon="el-icon-search" @click="loadList">筛选</el-button>
      </div>
    </div>

    <el-card shadow="hover" class="table-card">
      <el-table :data="tableData" border stripe style="width: 100%;">
        <el-table-column prop="messageId" label="编号" width="80" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="name" label="姓名" width="100" />
        <el-table-column prop="phone" label="电话" width="140" />
        <el-table-column prop="content" label="留言内容" show-overflow-tooltip />
        <el-table-column prop="reply" label="回复内容" show-overflow-tooltip />
        <el-table-column prop="createTime" label="提交时间" width="180" />
        <el-table-column prop="status" label="状态" width="100">
          <template slot-scope="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'warning'">
              {{ scope.row.status === 1 ? '已处理' : '未处理' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="260" fixed="right">
          <template slot-scope="scope">
            <el-button size="mini" type="primary" @click="openReply(scope.row)">回复</el-button>
            <el-button
              size="mini"
              :type="scope.row.status === 1 ? 'warning' : 'success'"
              @click="toggleStatus(scope.row)">
              {{ scope.row.status === 1 ? '标记未处理' : '标记已处理' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog title="回复留言" :visible.sync="replyVisible" width="520px">
      <el-input
        type="textarea"
        :rows="4"
        v-model="replyForm.reply"
        placeholder="请输入回复内容" />
      <div slot="footer">
        <el-button @click="replyVisible = false">取消</el-button>
        <el-button type="primary" @click="submitReply">提交回复</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getAdminContactMessageList, replyContactMessage, updateContactMessageStatus } from '@/api/ContactMessageAPI';

export default {
  name: 'AdminContactMessage',
  data() {
    return {
      status: null,
      tableData: [],
      replyVisible: false,
      replyForm: {
        messageId: null,
        reply: ''
      }
    }
  },
  async created() {
    await this.loadList();
  },
  methods: {
    async loadList() {
      const rows = await getAdminContactMessageList(this.status);
      this.tableData = Array.isArray(rows) ? rows : [];
    },
    openReply(row) {
      this.replyForm.messageId = row.messageId;
      this.replyForm.reply = row.reply || '';
      this.replyVisible = true;
    },
    async submitReply() {
      if (!this.replyForm.reply.trim()) {
        this.$message.warning('请输入回复内容');
        return;
      }
      const ok = await replyContactMessage(this.replyForm.messageId, this.replyForm.reply.trim());
      if (ok) {
        this.$message.success('回复成功');
        this.replyVisible = false;
        await this.loadList();
      } else {
        this.$message.error('回复失败');
      }
    },
    async toggleStatus(row) {
      const target = row.status === 1 ? 0 : 1;
      const ok = await updateContactMessageStatus(row.messageId, target);
      if (ok) {
        this.$message.success('状态更新成功');
        await this.loadList();
      } else {
        this.$message.error('状态更新失败');
      }
    }
  }
}
</script>

<style scoped>
.admin-contact-message {
  padding: 6px;
}
.toolbar-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 16px;
  margin-bottom: 12px;
  border-radius: 12px;
  background: linear-gradient(135deg, #f3f8ff, #f8fbff);
  border: 1px solid #e9f1ff;
}
.toolbar-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}
.table-card {
  border-radius: 12px;
}
</style>
