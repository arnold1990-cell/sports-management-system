import React, { useEffect, useState } from 'react';
import { Box, Button, Card, CardContent, TextField, Typography, Alert } from '@mui/material';
import { useParams } from 'react-router-dom';
import api from '../api/client';
import { useAuth } from '../context/AuthContext';

interface Post {
  id: string;
  title: string;
  content: string;
  coverImageUrl?: string;
  authorName?: string;
  createdAt: string;
}

interface Comment {
  id: string;
  content: string;
  authorId?: string;
  authorName?: string;
  createdAt: string;
}

const PostDetailPage: React.FC = () => {
  const { id } = useParams();
  const { accessToken, userId, roles } = useAuth();
  const [post, setPost] = useState<Post | null>(null);
  const [comments, setComments] = useState<Comment[]>([]);
  const [content, setContent] = useState('');
  const [error, setError] = useState<string | null>(null);

  const loadPost = async () => {
    if (!id) return;
    const response = await api.get(`/api/posts/published/${id}`);
    setPost(response.data);
  };

  const loadComments = async () => {
    if (!id) return;
    const response = await api.get(`/api/comments/post/${id}`);
    setComments(response.data.content || []);
  };

  useEffect(() => {
    loadPost();
    loadComments();
  }, [id]);

  const handleAddComment = async () => {
    if (!id) return;
    try {
      await api.post(`/api/comments/post/${id}`, { content });
      setContent('');
      loadComments();
    } catch (err: any) {
      setError(err?.response?.data?.message || 'Unable to add comment');
    }
  };

  const handleDelete = async (commentId: string) => {
    await api.delete(`/api/comments/${commentId}`);
    loadComments();
  };

  if (!post) {
    return <Typography>Loading post...</Typography>;
  }

  return (
    <Box>
      <Card sx={{ mb: 3 }}>
        {post.coverImageUrl && (
          <Box component="img" src={post.coverImageUrl} alt={post.title} sx={{ width: '100%', height: 260, objectFit: 'cover' }} />
        )}
        <CardContent>
          <Typography variant="h4" gutterBottom>{post.title}</Typography>
          <Typography variant="body2" gutterBottom>By {post.authorName || 'Admin'} on {new Date(post.createdAt).toLocaleDateString()}</Typography>
          <Typography variant="body1" sx={{ whiteSpace: 'pre-wrap' }}>{post.content}</Typography>
        </CardContent>
      </Card>
      <Box>
        <Typography variant="h6" gutterBottom>Comments</Typography>
        {comments.map((comment) => {
          const canDelete = roles.includes('ADMIN') || (userId && comment.authorId === userId);
          return (
            <Card key={comment.id} sx={{ mb: 2 }}>
              <CardContent>
                <Typography variant="subtitle2">{comment.authorName || 'User'} • {new Date(comment.createdAt).toLocaleString()}</Typography>
                <Typography variant="body2">{comment.content}</Typography>
                {canDelete && (
                  <Button size="small" color="error" onClick={() => handleDelete(comment.id)}>Delete</Button>
                )}
              </CardContent>
            </Card>
          );
        })}
        {accessToken ? (
          <Card>
            <CardContent sx={{ display: 'grid', gap: 2 }}>
              {error && <Alert severity="error">{error}</Alert>}
              <TextField label="Add a comment" multiline minRows={3} value={content} onChange={(e) => setContent(e.target.value)} />
              <Button variant="contained" onClick={handleAddComment}>Submit Comment</Button>
            </CardContent>
          </Card>
        ) : (
          <Typography variant="body2">Login to add a comment.</Typography>
        )}
      </Box>
    </Box>
  );
};

export default PostDetailPage;
