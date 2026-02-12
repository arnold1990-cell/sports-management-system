import React, { useMemo, useState } from 'react';
import {
  Alert,
  Box,
  Button,
  Card,
  CardContent,
  Chip,
  Grid,
  Stack,
  TextField,
  Typography,
} from '@mui/material';

type GuidanceForm = {
  studentName: string;
  gradeLevel: string;
  interests: string;
  strengths: string;
  targetCountry: string;
  preferredBudget: string;
};

const implementationStages = [
  {
    title: '1) Discovery & Student Intake',
    description:
      'Capture student profile data: academics, interests, strengths, financial constraints, and preferred study destination.',
  },
  {
    title: '2) Career Mapping Logic',
    description:
      'Map student profile to career clusters (tech, business, health, creative, public service) and rank recommended pathways.',
  },
  {
    title: '3) Prompting & AI Guidance Layer',
    description:
      'Use a structured prompt that asks the AI to return role suggestions, required skills, course options, timeline, and risks.',
  },
  {
    title: '4) Validation & Safety Guardrails',
    description:
      'Require confidence levels, include assumptions, and add a disclaimer that users should verify advice with counselors and local institutions.',
  },
  {
    title: '5) Action Plan & Follow-Up',
    description:
      'Generate a 30/60/90-day student action plan and set review checkpoints so recommendations can improve over time.',
  },
];

const basePrompt = (data: GuidanceForm) => `You are an AI Student Career Guidance Assistant.
Create personalized guidance for the student below.

Student profile:
- Name: ${data.studentName || 'N/A'}
- Grade level: ${data.gradeLevel || 'N/A'}
- Interests: ${data.interests || 'N/A'}
- Strengths: ${data.strengths || 'N/A'}
- Target country/region for study or work: ${data.targetCountry || 'N/A'}
- Budget constraints: ${data.preferredBudget || 'N/A'}

Return response in this structure:
1) Top 3 career path recommendations (with reasons)
2) Required skills for each path
3) Suggested degree/certification options
4) 6-month learning roadmap
5) Potential scholarships / low-cost options
6) Risks and mitigation tips
7) 30/60/90 day action plan

Rules:
- Be realistic, concise, and student-friendly.
- Mention assumptions explicitly.
- Include one paragraph of motivational feedback.
- End with: "Please validate final decisions with a qualified academic/career counselor."`;

const CareerGuidancePage: React.FC = () => {
  const [form, setForm] = useState<GuidanceForm>({
    studentName: '',
    gradeLevel: '',
    interests: '',
    strengths: '',
    targetCountry: '',
    preferredBudget: '',
  });
  const [copyStatus, setCopyStatus] = useState<string>('');

  const prompt = useMemo(() => basePrompt(form), [form]);

  const setField = (field: keyof GuidanceForm) => (event: React.ChangeEvent<HTMLInputElement>) => {
    setForm((previous) => ({ ...previous, [field]: event.target.value }));
    setCopyStatus('');
  };

  const handleCopy = async () => {
    await navigator.clipboard.writeText(prompt);
    setCopyStatus('Prompt copied. Paste it into your AI tool (ChatGPT, Claude, Gemini, etc.).');
  };

  return (
    <Stack spacing={3}>
      <Typography variant="h4">AI Student Career Guidance Starter</Typography>
      <Typography color="text.secondary">
        Fill in the student details, copy the generated prompt, and paste it into your preferred AI assistant.
      </Typography>

      <Card>
        <CardContent>
          <Typography variant="h6" gutterBottom>
            Student Input Form
          </Typography>
          <Grid container spacing={2}>
            <Grid item xs={12} md={6}>
              <TextField fullWidth label="Student Name" value={form.studentName} onChange={setField('studentName')} />
            </Grid>
            <Grid item xs={12} md={6}>
              <TextField fullWidth label="Grade Level" value={form.gradeLevel} onChange={setField('gradeLevel')} />
            </Grid>
            <Grid item xs={12} md={6}>
              <TextField fullWidth label="Interests (comma separated)" value={form.interests} onChange={setField('interests')} />
            </Grid>
            <Grid item xs={12} md={6}>
              <TextField fullWidth label="Strengths" value={form.strengths} onChange={setField('strengths')} />
            </Grid>
            <Grid item xs={12} md={6}>
              <TextField fullWidth label="Target Country/Region" value={form.targetCountry} onChange={setField('targetCountry')} />
            </Grid>
            <Grid item xs={12} md={6}>
              <TextField fullWidth label="Budget Constraints" value={form.preferredBudget} onChange={setField('preferredBudget')} />
            </Grid>
          </Grid>
        </CardContent>
      </Card>

      <Card>
        <CardContent>
          <Stack direction="row" justifyContent="space-between" alignItems="center" mb={2}>
            <Typography variant="h6">Copy/Paste Prompt</Typography>
            <Button variant="contained" onClick={handleCopy}>Copy Prompt</Button>
          </Stack>
          <TextField multiline minRows={14} fullWidth value={prompt} InputProps={{ readOnly: true }} />
          {copyStatus && (
            <Box mt={2}>
              <Alert severity="success">{copyStatus}</Alert>
            </Box>
          )}
        </CardContent>
      </Card>

      <Card>
        <CardContent>
          <Typography variant="h6" gutterBottom>
            Necessary Implementation Stages
          </Typography>
          <Stack spacing={2}>
            {implementationStages.map((stage) => (
              <Box key={stage.title}>
                <Chip label={stage.title} color="primary" sx={{ mb: 1 }} />
                <Typography color="text.secondary">{stage.description}</Typography>
              </Box>
            ))}
          </Stack>
        </CardContent>
      </Card>
    </Stack>
  );
};

export default CareerGuidancePage;
