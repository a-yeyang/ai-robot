<template>
    <!-- 主内容区域 -->
    <div class="h-screen flex flex-col overflow-y-auto" ref="chatContainer">
        
        <a-tooltip placement="right">
          <!-- Tooltip 提示文字 -->
          <template #title>
            <span>返回首页</span>
          </template>
          <!-- 返回首页按钮 -->
          <div class="fixed top-4 left-4 z-10">
            <button class="w-10 h-10 rounded-full bg-white border border-gray-200 flex items-center justify-center cursor-pointer 
            shadow-xs hover:bg-gray-100 transition-all duration-200" @click="jumpHomePage">
                <SvgIcon name="back" customCss="w-5 h-5 text-gray-500"></SvgIcon>
            </button>
          </div>
        </a-tooltip>

      <!-- 右下角浮动设置按钮 -->
      <div class="fixed top-4 right-4 z-10">
        <a-tooltip placement="left">
          <!-- Tooltip 提示文字 -->
          <template #title>
            <span>设置</span>
          </template>
          <!-- 设置按钮 -->
          <button class="w-10 h-10 rounded-full bg-white border border-gray-200 flex items-center justify-center cursor-pointer 
          shadow-xs hover:bg-gray-100 hover:shadow-xl transition-all duration-200" @click="showSettingsDrawer">
              <SvgIcon name="settings" customCss="w-5 h-5 text-gray-500"></SvgIcon>
          </button>
        </a-tooltip>
      </div>
        

        <!-- 聊天记录区域 -->
        <div class="flex-1 max-w-3xl mx-auto pb-24 pt-4 px-4 w-full">
          <!-- 遍历聊天记录 -->
          <template v-for="(chat, index) in chatList" :key="index">
            <!-- 用户提问消息（靠右） -->
            <div v-if="chat.role === 'user'" class="flex justify-end mb-4">
              <div class="quesiton-container">
                <p>{{ chat.content }}</p>
              </div>
            </div>

            <!-- 大模型回复消息（靠左） -->
            <div v-else class="flex mb-4">
              <!-- 头像 -->
              <div class="flex-shrink-0 mr-3">
                <div class="w-8 h-8 rounded-full flex items-center justify-center border border-gray-200">
                  <SvgIcon name="customer-service-logo" customCss="w-5 h-5"></SvgIcon>
                </div>
              </div>
              <!-- 回复的内容 -->
              <div class="p-1 mb-2 max-w-[90%]">
                <LoadingDots v-if="chat.loading" />
                <StreamMarkdownRender :content="chat.content" />
              </div>
            </div>
          </template>
        </div>

        <!-- 提问输入框 -->
        <ChatInputBox v-model="chatMessage" containerClass="sticky max-w-3xl mx-auto bg-white bottom-8 left-0 w-full"
          @sendMessage="sendMessage" placeholder="向小哈 AI 智能客服询问" :showModelDropdown="false" :showNetworkSearch="false"/>
      </div>

      <!-- 抽屉：客服问答文件管理 -->
      <a-drawer width="80%" title="客服问答文件管理" placement="right" :open="settingsDrawerOpen" @close="onSettingsDrawerClose">
          <!-- 搜索区域 -->
          <div class="mb-5">
            <a-form
              layout="inline"
              :model="formState"
            >
              <a-form-item label="文件名称：" name="fileName">
                <a-input v-model:value="formState.fileName" placeholder="请输入文件名称（模糊查询）" style="width: 230px" allowClear>
                </a-input>
              </a-form-item>
              <a-form-item label="创建时间：" name="startEndDate">
                <a-range-picker v-model:value="formState.startEndDate" />
              </a-form-item>
              <a-button type="primary" :icon="h(SearchOutlined)" class="mr-3" @click="handleSearch">查询</a-button>
              <a-button :icon="h(RedoOutlined)" @click="handleReset">重置</a-button>
            </a-form>
            <!-- 分割线 -->
            <a-divider />
          </div>
          <a-button type="primary" class="mb-5" :icon="h(UploadOutlined)" @click="triggerFileUpload" :loading="uploadBtnLoading">上传 Markdown 文件</a-button>

          <!-- 隐藏的 file input 元素 -->
          <input 
            type="file" 
            ref="fileInput" 
            @change="handleFileSelect" 
            class="hidden"
            accept=".md,.markdown,text/markdown"
          />

          <a-table :dataSource="dataSource" :columns="columns" :pagination="pagination" @change="handleTableChange" :loading="loading">
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'action'">
                <span>
                  <a-button @click="showEditMarkdownModel(record)">编辑</a-button>
                  <a-divider type="vertical" />
                  <a-button danger @click="showDeleteMarkdownModel(record.id)">删除</a-button>
                </span>
              </template>
              <template v-else-if="column.key === 'status'">
                <span>
                  <a-tag color="default" v-if="record.status === 0">待处理</a-tag>
                  <a-tag color="processing" v-else-if="record.status === 1">向量化中</a-tag>
                  <a-tag color="success" v-else-if="record.status === 2">已完成</a-tag>
                  <a-tag color="error" v-else-if="record.status === 3">失败</a-tag>
                </span>
              </template>
            </template>
          </a-table>
      </a-drawer>

      <!-- 删除 Markdown 问答文件确认框 -->
      <a-modal v-model:open="deleteMarkdownModelOpen" width="400px" :centered=true title="永久删除问答文件" ok-text="确认" ok-type="danger" cancel-text="取消"
      @ok="handleDeleteMarkdownModelOk()">
          <p>是否确认删除此 Markdown 问答文件？</p>
      </a-modal>

      <!-- 编辑 Markdown 问答文件模态框 -->
      <a-modal v-model:open="editMarkdownModelOpen" width="700px" :centered=true title="编辑问答文件" ok-text="提交" cancel-text="取消"
      @ok="handleEditMarkdownModelOk()">
        <div class="mt-7"></div>
        <a-form
          :model="editMarkdownRecord"
          :label-col="{ span: 4 }"
          :wrapper-col="{ span: 20 }"
          autocomplete="off"
        >
          <a-form-item
            label="ID"
            name="id"
          >
            <a-input v-model:value="editMarkdownRecord.id" disabled/>
          </a-form-item>
          <a-form-item
            label="文件名"
            name="originalFileName"
          >
            <a-input v-model:value="editMarkdownRecord.originalFileName" disabled/>
          </a-form-item>
          <a-form-item
            label="文件大小"
            name="fileSize"
          >
            <a-input v-model:value="editMarkdownRecord.fileSize" disabled/>
          </a-form-item>
          <a-form-item
            label="处理状态"
            name="status"
          >
                  <a-tag color="default" v-if="editMarkdownRecord.status === 0">待处理</a-tag>
                  <a-tag color="processing" v-else-if="editMarkdownRecord.status === 1">向量化中</a-tag>
                  <a-tag color="success" v-else-if="editMarkdownRecord.status === 2">已完成</a-tag>
                  <a-tag color="error" v-else-if="editMarkdownRecord.status === 3">失败</a-tag>
          </a-form-item>
          <a-form-item
            label="创建时间"
            name="createTime"
          >
            <a-input v-model:value="editMarkdownRecord.createTime" disabled/>
          </a-form-item>
          <a-form-item
            label="更新时间"
            name="updateTime"
          >
            <a-input v-model:value="editMarkdownRecord.updateTime" disabled/>
          </a-form-item>
          <a-form-item
            label="备注"
            name="remark"
          >
            <a-textarea v-model:value="editMarkdownRecord.remark" 
            placeholder="请输入备注信息"
            :auto-size="{ minRows: 5, maxRows: 10 }"
            allow-clear show-count />
          </a-form-item>
        </a-form>
      </a-modal>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, nextTick, watch, h, computed, reactive } from 'vue'
import SvgIcon from '@/components/SvgIcon.vue'
import StreamMarkdownRender from '@/components/StreamMarkdownRender.vue'
import LoadingDots from '@/components/LoadingDots.vue'
import ChatInputBox from '@/components/ChatInputBox.vue'
import { useRoute, useRouter } from 'vue-router'
import { fetchEventSource } from '@microsoft/fetch-event-source'
import { UploadOutlined, SearchOutlined, RedoOutlined } from '@ant-design/icons-vue'
import { findMarkdownFilePageList, uploadMarkdownFile, deleteMarkdownFile, updateMarkdownFile } from '@/api/customerService'
import { message } from 'ant-design-vue'

console.log('首页传递过来的消息: ', history.state?.firstMessage)

const route = useRoute()
const router = useRouter()

// 返回首页
const jumpHomePage = () => {
  router.push({ name: 'Index' })
}

// 输入的消息
const chatMessage = ref(history.state?.firstMessage || '')

// 聊天容器引用
const chatContainer = ref(null)

// 聊天记录，默认给一个欢迎语
const chatList = ref([{ role: 'assistant', content: '你好呀！我是 “犬小哈项目实战专栏” 的 AI 智能客服，基于 Spring AI 开发，欢迎向我咨询项目相关问题哈 😁', loading: false }])

// 对话 ID
const chatId = ref(null)

// 发送消息
const sendMessage = async () => {
  // 校验发送的消息不能为空
  if (!chatMessage.value.trim()) return

  // 将用户发送的消息添加到 chatList 聊天列表中
  const userMessage = chatMessage.value.trim()
  chatList.value.push({ role: 'user', content: userMessage })

  // 点击发送按钮后，清空输入框
  chatMessage.value = ''

  // 添加一个占位的回复消息
  chatList.value.push({ role: 'assistant', content: '', loading: true})

  try {
    // 构建请求体
    const requestBody = {
      message: userMessage,
      chatId: chatId.value,
    }

    // 响应的回答
    let responseText = ''
    // 获取最后一条消息
    const lastMessage = chatList.value[chatList.value.length - 1]

    const controller = new AbortController()
    const signal = controller.signal

    fetchEventSource('http://localhost:8080/customer-service/completion', {
      method: 'POST',
      signal: signal,
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(requestBody),
      onmessage(msg) {
        if (msg.event === '') {
          // 收到第一条数据后设置 loading 为 false
          if (lastMessage.loading) {
              lastMessage.loading = false;
          }
          // 解析 JSON
          let parseJson = JSON.parse(msg.data)
          // 持续追加流式回答
          responseText += parseJson.v

          // 更新最后一条消息
          chatList.value[chatList.value.length - 1].content = responseText
          // 滚动到底部
          scrollToBottom()
        }
        else if (msg.event === 'close') {
          console.log('-- sse close')
          controller.abort();
        }
      },
      onerror(err) {
        throw err;    // 必须 throw 才能停止 
      }
    })
  } catch (error) {
    console.error('发送消息错误: ', error)
    // 提示用户 “请求出错”
    const lastMessage = chatList.value[chatList.value.length - 1]
    lastMessage.content = '抱歉，请求出错了，请稍后重试。'
    lastMessage.loading = false
    // 滚动到底部
    scrollToBottom()
  }
}

// 滚动到底部
const scrollToBottom = async () => {
  await nextTick() // 等待 Vue.js 完成 DOM 更新
  if (chatContainer.value) { // 若容器存在
    // 将容器的滚动条位置设置到最底部
    const container = chatContainer.value;
    container.scrollTop = container.scrollHeight;
  }
}

// 设置抽屉是否展示
const settingsDrawerOpen = ref(false)

// 渲染 a-table 表格数据
const renderTableData = (pageNo, pageSize) => {
  // 设置 a-table 组件加载状态为 true
  loading.value = true

  const [startDate, endDate] = formState.startEndDate
  const startDateStr = startDate?.format('YYYY-MM-DD')
  const endDateStr = endDate?.format('YYYY-MM-DD')

  // 打印日志
  console.log(formState.fileName)
  console.log(startDateStr)
  console.log(endDateStr)

  // 请求分页接口
  findMarkdownFilePageList(pageNo, pageSize, formState.fileName, startDateStr, endDateStr).then(res => {
      if (res.data.success) {
        dataSource.value = res.data.data
        current.value = res.data.current
        total.value = res.data.total
      }
  }).finally(() => {
    // 设置 a-table 组件加载状态为 false
    loading.value = false    
  })
}

// 打开设置抽屉
const showSettingsDrawer = () => {
  settingsDrawerOpen.value = true

  renderTableData(current.value, pageSize.value)
}

// 关闭设置抽屉
const onSettingsDrawerClose = () => {
  settingsDrawerOpen.value = false
}

// 表格列
const columns = ref([
  {
    title: '文件名称',
    dataIndex: 'originalFileName',
    key: 'originalFileName',
  },
  {
    title: '文件大小',
    dataIndex: 'fileSize',
    key: 'fileSize',
  },
  {
    title: '状态',
    dataIndex: 'status',
    key: 'status',
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
    key: 'createTime',
  },
  {
    title: '更新时间',
    dataIndex: 'updateTime',
    key: 'updateTime',
  },
  {
    title: '备注',
    dataIndex: 'remark',
    key: 'remark',
  },
  {
    title: '操作',
    key: 'action',
  },
])

// 表格数据
const dataSource = ref([])

// 分页数据
// 当前页码
const current = ref(1)
// 每页展示数据量
const pageSize = ref(10)
// 总数据量
const total = ref(0)
// 表格加载状态
const loading = ref(false)

const pagination = computed(() => ({
  total: total.value,
  current: current.value,
  pageSize: pageSize.value
}))

// 表格变化监听事件
const handleTableChange = (pageObj) => {
    renderTableData(pageObj.current, pageObj.pageSize)
}

// 搜索表单
const formState = reactive({
  fileName: '', // 文件名称
  startEndDate: [] // 起始、结束日期
})

// 搜索按钮点击事件
const handleSearch = () => {
  renderTableData(current.value, pageSize.value)
}

// 重置搜索表单
const handleReset = () => {
  formState.fileName = ''
  formState.startEndDate = []

  // 重置表单后，渲染第一页的数据
  renderTableData(1, pageSize.value)
}

// 隐藏的文件上传 input 引用
const fileInput = ref(null)

// 触发文件选择对话框
const triggerFileUpload = () => {
  fileInput.value.click()
}

// 上传文件按钮 Loading 动画
const uploadBtnLoading = ref(false)

// 处理文件选择
const handleFileSelect = (event) => {
  const file = event.target.files[0]
  if (file) {
    console.log('已选择 Markdown 文件:', file.name)

    // 表单对象
    let formData = new FormData()
    // 添加 file 字段，并将文件传入 
    formData.append('file', file)

    // 显示上传按钮的 Loading 动画
    uploadBtnLoading.value = true

    uploadMarkdownFile(formData).then((res) => {
        // 响参失败，提示错误消息
        if (!res.data.success) {
          message.warning(res.data.message)
          return
        }

        message.success('上传成功')

        // 重新渲染列表数据
        renderTableData(1, pageSize.value)
    }).finally(() => {
      // 隐藏上传按钮的 Loading 动画
      uploadBtnLoading.value = false
    })
  }
}

// 是否展示 “删除 Markdown 文件” 确认框
const deleteMarkdownModelOpen = ref(false)

// 被删除的文件记录 ID
const deleteMarkdownId = ref(null)

// 展示 “删除 Markdown 文件” 确认框
const showDeleteMarkdownModel = (id) => {
  // 展示确认框
  deleteMarkdownModelOpen.value = true
  // 保存被删除的文件记录 ID
  deleteMarkdownId.value = id

  console.log('当前被删除的 Markdown 文件 ID 为：' + deleteMarkdownId.value)
}

// “删除 Markdown 文件” 确认框确认按钮点击事件
const handleDeleteMarkdownModelOk = () => {
  // 调用后端删除接口
  deleteMarkdownFile(deleteMarkdownId.value).then(res => {
        // 响参失败，提示错误消息
        if (!res.data.success) {
          message.warning(res.data.message)
          return
        }

        message.success('删除成功')

        // 隐藏确认框
        deleteMarkdownModelOpen.value = false

        // 重新渲染列表数据
        renderTableData(1, pageSize.value)
  })
}

// 是否展示 “编辑 Markdown 文件” 模态框
const editMarkdownModelOpen = ref(false)
// 被编辑的文件记录
const editMarkdownRecord = reactive({
  id: null,
  originalFileName: '',
  fileSize: '',
  status: null,
  createTime: '',
  updateTime: '',
  remark: ''
})

// 展示 “编辑 Markdown 文件” 模态框
const showEditMarkdownModel = (record) => {
  // 展示模态框
  editMarkdownModelOpen.value = true
  // 保存被编辑的文件记录
  editMarkdownRecord.id = record.id
  editMarkdownRecord.originalFileName = record.originalFileName
  editMarkdownRecord.fileSize = record.fileSize
  editMarkdownRecord.status = record.status
  editMarkdownRecord.createTime = record.createTime
  editMarkdownRecord.updateTime = record.updateTime
  editMarkdownRecord.remark = record.remark

  console.log('当前被编辑的 Markdown 文件记录为：')
  console.log(editMarkdownRecord)
}

// 编辑模态框 “提交” 按钮事件
const handleEditMarkdownModelOk = () => {
  updateMarkdownFile(editMarkdownRecord).then(res => {
        // 响参失败，提示错误消息
        if (!res.data.success) {
          message.warning(res.data.message)
          return
        }

        message.success('更新成功')

        // 隐藏模态框
        editMarkdownModelOpen.value = false

        // 重新渲染列表数据
        renderTableData(current.value, pageSize.value)
  })
}
</script>

<style scoped>
.quesiton-container {
  font-size: 16px;
  line-height: 28px;
  color: #262626;
  padding: calc((44px - 28px) / 2) 20px;
  box-sizing: border-box;
  white-space: pre-wrap;
  word-break: break-word;
  background-color: #eff6ff;
  border-radius: 14px;
  max-width: calc(100% - 48px);
  position: relative;
}

/* 聊天内容区域样式 */
.overflow-y-auto {
  scrollbar-color: rgba(0, 0, 0, 0.2) transparent;
  /* 自定义滚动条颜色 */
}

/* 解决按钮中图标没有垂直居中问题 */
:where(.css-dev-only-do-not-override-1p3hq3p).ant-btn >span {
    display: inline-flex;
}
</style>