import api from "./axios";

const buildParams = ({ page = 0, size = 9, keywords = "", category = "" } = {}) => ({
  page,
  size,
  keywords: keywords || undefined,
  category: category || undefined,
});

const buildFormData = ({
  title,
  category,
  content,
  published,
  files = [],
  attachmentsToDelete = [],
}) => {
  const formData = new FormData();
  const data = new Blob(
    [
      JSON.stringify({
        title,
        category,
        content,
        published,
      }),
    ],
    { type: "application/json" }
  );

  formData.append("data", data);
  files.forEach((file) => formData.append("files", file));
  attachmentsToDelete.forEach((attachmentId) =>
    formData.append("attachmentsToDelete", attachmentId)
  );

  return formData;
};

export const findPublishedPosts = (filters) =>
  api.get("/posts", { params: buildParams(filters) });

export const findAdminPosts = (filters) =>
  api.get("/posts/admin", { params: buildParams(filters) });

export const findPublishedPostById = (postId) => api.get(`/posts/${postId}`);

export const findAdminPostById = (postId) => api.get(`/posts/admin/${postId}`);

export const createPost = (post) => api.post("/posts", buildFormData(post));

export const updatePost = (postId, post) =>
  api.put(`/posts/${postId}`, buildFormData(post));

export const deletePost = (postId) => api.delete(`/posts/${postId}`);

export const setPostPublished = (postId, value) =>
  api.patch(`/posts/${postId}/published`, null, { params: { value } });

export const downloadPostAttachment = (attachmentId) =>
  api.get(`/posts/attachments/${attachmentId}/download`, { responseType: "blob" });

export const getPostAttachmentUrl = (attachmentId) =>
  `${api.defaults.baseURL}/posts/attachments/${attachmentId}/download`;
